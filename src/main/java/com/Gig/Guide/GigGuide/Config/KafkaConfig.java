package com.Gig.Guide.GigGuide.Config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

/**
 * Kafka configuration — makes the app resilient when Kafka is unavailable.
 *
 * Key behaviours:
 *  - Consumer error handler: if a message fails to process, retry up to 3 times
 *    with a 2-second gap before giving up on that specific message (not the whole listener).
 *  - AckMode.BATCH: only commit offsets when a full batch processes cleanly.
 *  - The consumer container keeps running and reconnects automatically when
 *    Kafka comes back — no app restart needed.
 *
 * The noisy WARN logs are handled in application.properties by setting
 * NetworkClient to ERROR level and adding reconnect backoff settings.
 */
@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Customises the listener container factory used by @KafkaListener.
     *
     * DefaultErrorHandler with FixedBackOff(2000ms, 3 attempts):
     *   - If message processing throws, it retries up to 3 times, 2s apart.
     *   - After 3 failures the message is skipped (logged as error) and the
     *     consumer moves on — it does NOT stop the whole listener.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // Commit offsets after each batch is processed cleanly
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

        // Retry failed messages up to 3 times with a 2s gap, then log and skip
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, ex) -> log.error(
                        "Message permanently failed after retries — topic={}, partition={}, offset={}, error={}",
                        record.topic(), record.partition(), record.offset(), ex.getMessage()
                ),
                new FixedBackOff(2_000L, 3L)
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    /**
     * AdminClient bean — used by KafkaHealthIndicator to probe broker connectivity.
     * Short timeout so health checks fail fast instead of blocking.
     */
    @Bean
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000",
                AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "3000"
        ));
    }
}
