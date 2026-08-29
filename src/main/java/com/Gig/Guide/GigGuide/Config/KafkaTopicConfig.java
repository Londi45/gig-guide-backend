package com.Gig.Guide.GigGuide.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares Kafka topics as Spring beans.
 *
 * Spring Boot's KafkaAdmin picks these up on startup and creates them
 * on the broker if they don't already exist — no manual kafka-topics.sh needed.
 *
 * partitions=3  — allows 3 consumers to process in parallel
 * replicas=1    — single broker locally, increase for production
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.club-created}")
    private String clubCreatedTopic;

    @Bean
    public NewTopic clubCreatedTopic() {
        return TopicBuilder.name(clubCreatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
