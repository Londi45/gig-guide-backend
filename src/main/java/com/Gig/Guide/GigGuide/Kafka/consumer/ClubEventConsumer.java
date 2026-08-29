package com.Gig.Guide.GigGuide.Kafka.consumer;

import com.Gig.Guide.GigGuide.Kafka.events.ClubCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer — listens for club lifecycle events.
 *
 * @KafkaListener subscribes this method to the "club-created" topic.
 * Kafka will automatically call onClubCreated() whenever a new message
 * arrives on that topic for the consumer group "gig-guide-group".
 *
 * Consumer group: all consumers in the same group share the work.
 * If you had 2 instances of this app running, Kafka would split
 * the topic partitions between them so each message is only processed once.
 */
@Slf4j
@Service
public class ClubEventConsumer {

    @KafkaListener(
            topics = "${app.kafka.topic.club-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onClubCreated(
            @Payload ClubCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received ClubCreatedEvent - clubId={}, name={}, email={}, city={}, partition={}, offset={}",
                event.getClubId(),
                event.getClubName(),
                event.getEmail(),
                event.getCity(),
                partition,
                offset);

        // ─── This is where you add your downstream logic ──────────────────
        // Examples:
        //   emailService.sendWelcomeEmail(event.getEmail(), event.getClubName());
        //   cacheService.invalidateClubListCache();
        //   notificationService.notifyAdmins(event);
        // ─────────────────────────────────────────────────────────────────
    }
}
