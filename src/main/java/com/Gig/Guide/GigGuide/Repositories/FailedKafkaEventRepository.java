package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Models.Kafka.FailedKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedKafkaEventRepository extends JpaRepository<FailedKafkaEvent, Long> {

    // Load all unresolved failures — used by a retry job or admin endpoint
    List<FailedKafkaEvent> findByResolvedFalseOrderByFailedAtAsc();

    // Load failures for a specific topic
    List<FailedKafkaEvent> findByTopicAndResolvedFalseOrderByFailedAtAsc(String topic);
}
