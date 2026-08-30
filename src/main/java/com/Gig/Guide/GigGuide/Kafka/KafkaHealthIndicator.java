package com.Gig.Guide.GigGuide.Kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Exposes Kafka broker reachability at /actuator/health.
 *
 * Also acts as a fast in-process flag (isAvailable()) so the producer
 * can skip the network call entirely when Kafka is known to be down,
 * going straight to the DB fallback without any delay.
 *
 * The flag is refreshed every time Spring Boot polls the health endpoint
 * (default: every 10 seconds when caching is enabled).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.kafka.listener.auto-startup", havingValue = "true", matchIfMissing = true)
public class KafkaHealthIndicator implements HealthIndicator {

    @Autowired
    private AdminClient kafkaAdminClient;

    // Optimistic default — assumes Kafka is up until proven otherwise
    private final AtomicBoolean available = new AtomicBoolean(true);

    @Override
    public Health health() {
        try {
            // listTopics with a 3-second timeout — fast probe
            kafkaAdminClient.listTopics()
                    .names()
                    .get(3, TimeUnit.SECONDS);

            if (!available.get()) {
                log.info("Kafka is back online");
            }
            available.set(true);
            return Health.up().withDetail("broker", "reachable").build();

        } catch (Exception ex) {
            if (available.get()) {
                log.warn("Kafka is unreachable — producer will fall back to DB storage. Reason: {}", ex.getMessage());
            }
            available.set(false);
            return Health.down()
                    .withDetail("broker", "unreachable")
                    .withDetail("reason", ex.getMessage())
                    .build();
        }
    }

    /**
     * Fast non-blocking check used by the producer before attempting to send.
     * Returns the last known state — no network call.
     */
    public boolean isAvailable() {
        return available.get();
    }
}
