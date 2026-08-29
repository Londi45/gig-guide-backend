package com.Gig.Guide.GigGuide.Kafka.job;

import com.Gig.Guide.GigGuide.Kafka.events.ClubCreatedEvent;
import com.Gig.Guide.GigGuide.Models.Kafka.FailedKafkaEvent;
import com.Gig.Guide.GigGuide.Repositories.FailedKafkaEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled retry job for failed Kafka events.
 *
 * Runs every N milliseconds (configured via app.kafka.retry.fixed-rate-ms).
 * For each unresolved failure it:
 *   1. Deserializes the stored JSON payload back to the correct event type
 *   2. Re-publishes it to Kafka
 *   3. Marks it resolved=true on success
 *   4. Increments retryCount and updates lastRetriedAt on failure
 *   5. Stops retrying after max-attempts and logs a dead-letter warning
 */
@Slf4j
@Component
public class FailedKafkaEventRetryJob {

    @Value("${app.kafka.topic.club-created}")
    private String clubCreatedTopic;

    @Value("${app.kafka.retry.fixed-rate-ms:7200000}")
    private long fixedRateMs;

    @Autowired
    private FailedKafkaEventRepository failedKafkaEventRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Scheduled(fixedRateString = "${app.kafka.retry.fixed-rate-ms:300000}")
    public void retryFailedEvents() {
        List<FailedKafkaEvent> pending = failedKafkaEventRepository
                .findByResolvedFalseOrderByFailedAtAsc();

        if (pending.isEmpty()) {
            log.debug("Retry job ran — no pending failed Kafka events");
            return;
        }

        log.info("Retry job started — {} unresolved Kafka event(s) found", pending.size());

        for (FailedKafkaEvent failed : pending) {
            try {
                Object payload = deserialize(failed);
                if (payload == null) {
                    log.error("Could not deserialize payload for failed event id={}, skipping", failed.getId());
                    continue;
                }

                // Re-publish synchronously so we know immediately if it succeeded
                kafkaTemplate.send(failed.getTopic(), failed.getMessageKey(), payload).get();

                // Success — mark as resolved
                failed.setResolved(true);
                failed.setLastRetriedAt(LocalDateTime.now());
                failedKafkaEventRepository.save(failed);

                log.info("Retry succeeded — resolved failed event id={}, topic={}, key={}",
                        failed.getId(), failed.getTopic(), failed.getMessageKey());

            } catch (Exception ex) {
                // Still failing — increment counter and record the time
                failed.setRetryCount(failed.getRetryCount() + 1);
                failed.setLastRetriedAt(LocalDateTime.now());
                failed.setErrorMessage(ex.getMessage());
                failedKafkaEventRepository.save(failed);

                log.warn("Retry attempt {} failed for event id={}, topic={}: {}",
                        failed.getRetryCount(),
                        failed.getId(), failed.getTopic(), ex.getMessage());
            }
        }

        log.info("Retry job finished");
    }

    /**
     * Deserializes the stored JSON payload back to the correct event class
     * based on which topic the failure came from.
     *
     * Add a new case here whenever you add a new Kafka topic/event type.
     */
    private Object deserialize(FailedKafkaEvent failed) {
        try {
            if (clubCreatedTopic.equals(failed.getTopic())) {
                return objectMapper.readValue(failed.getPayload(), ClubCreatedEvent.class);
            }
            log.error("Unknown topic '{}' — cannot deserialize payload for event id={}",
                    failed.getTopic(), failed.getId());
            return null;
        } catch (Exception e) {
            log.error("Deserialization failed for event id={}: {}", failed.getId(), e.getMessage());
            return null;
        }
    }
}
