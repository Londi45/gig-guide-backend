package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Models.Event.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByEventId(Long eventId);

    List<Discount> findByEventIdAndValidFromBeforeAndValidUntilAfter(
            Long eventId, LocalDateTime now1, LocalDateTime now2);
}
