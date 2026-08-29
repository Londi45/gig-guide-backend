package com.Gig.Guide.GigGuide.Kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event payload published to the "club-created" Kafka topic
 * whenever a new club is successfully created.
 *
 * This is the message that travels through Kafka — it gets serialized
 * to JSON by the producer and deserialized back to this object by the consumer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubCreatedEvent implements Serializable {

    private String clubId;
    private String clubName;
    private String email;
    private String city;
    private LocalDateTime createdAt;
}
