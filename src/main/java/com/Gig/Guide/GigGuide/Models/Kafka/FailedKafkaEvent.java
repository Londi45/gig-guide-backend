package com.Gig.Guide.GigGuide.Models.Kafka;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Persisted record of a Kafka publish failure.
 *
 * When the producer fails to deliver an event to Kafka, instead of
 * silently losing it we save it here. This gives you a retry mechanism
 * and a full audit trail of what failed and why.
 */
@Entity
@Table(name = "failed_kafka_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedKafkaEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which Kafka topic the message was intended for
    @Column(nullable = false)
    private String topic;

    // The message key (e.g. clubId as a string)
    private String messageKey;

    // The full event payload serialized as JSON
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    // The error message from Kafka
    @Column(nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    // Number of retry attempts made so far
    @Builder.Default
    private int retryCount = 0;

    // Whether the event has been successfully retried and published
    @Builder.Default
    private boolean resolved = false;

    // When the failure first occurred
    @Column(nullable = false)
    private LocalDateTime failedAt;

    // When the last retry attempt was made
    private LocalDateTime lastRetriedAt;
}
