package com.Gig.Guide.GigGuide.Kafka.producer;

import com.Gig.Guide.GigGuide.Kafka.KafkaHealthIndicator;
import com.Gig.Guide.GigGuide.Kafka.events.ClubCreatedEvent;
import com.Gig.Guide.GigGuide.Models.Kafka.FailedKafkaEvent;
import com.Gig.Guide.GigGuide.Repositories.FailedKafkaEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer — publishes club lifecycle events to Kafka topics.
 *
 * If Kafka is known to be unreachable (via KafkaHealthIndicator), the event
 * is saved directly to failed_kafka_events without attempting a network call.
 * The retry job will re-publish it once Kafka is back.
 */
@Slf4j
@Service
public class ClubEventProducer {

    @Value("${app.kafka.topic.club-created}")
    private String clubCreatedTopic;

    @Autowired
    private KafkaTemplate<String, ClubCreatedEvent> kafkaTemplate;

    @Autowired
    private FailedKafkaEventRepository failedKafkaEventRepository;

    @Autowired
    private KafkaHealthIndicator kafkaHealthIndicator;

    // ObjectMapper for serializing the event payload to JSON string for storage
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Publishes a ClubCreatedEvent to the "club-created" topic.
     *
     * Fast-path: if Kafka is known to be down, skip the network call entirely
     * and persist directly to failed_kafka_events for later retry.
     */
    public void publishClubCreated(ClubCreatedEvent event) {
        if (!kafkaHealthIndicator.isAvailable()) {
            log.warn("Kafka unavailable — persisting ClubCreatedEvent to DB for retry. clubId={}",
                    event.getClubId());
            saveFailedEvent(clubCreatedTopic, event.getClubId(), event, "Kafka unavailable at publish time");
            return;
        }

        log.info("Publishing ClubCreatedEvent to topic={} - clubId={}, name={}",
                clubCreatedTopic, event.getClubId(), event.getClubName());

        CompletableFuture<SendResult<String, ClubCreatedEvent>> future =
                kafkaTemplate.send(clubCreatedTopic, event.getClubId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("ClubCreatedEvent delivered - clubId={}, partition={}, offset={}",
                        event.getClubId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to deliver ClubCreatedEvent - clubId={}, error={}",
                        event.getClubId(), ex.getMessage());
                saveFailedEvent(clubCreatedTopic, event.getClubId(), event, ex.getMessage());
            }
        });
    }

    /**
     * Persists a failed event to the database.
     * Called from the async callback — runs on a Kafka thread, not the HTTP thread.
     */
    private void saveFailedEvent(String topic, String key, Object event, String errorMessage) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            FailedKafkaEvent failed = FailedKafkaEvent.builder()
                    .topic(topic)
                    .messageKey(key)
                    .payload(payload)
                    .errorMessage(errorMessage)
                    .retryCount(0)
                    .resolved(false)
                    .failedAt(LocalDateTime.now())
                    .build();

            failedKafkaEventRepository.save(failed);
            log.warn("Saved failed Kafka event to DB - topic={}, key={}", topic, key);

        } catch (JsonProcessingException e) {
            log.error("Could not serialize failed event payload for topic={}, key={}: {}",
                    topic, key, e.getMessage());
        }
    }
}
