package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Enums.EventStatus;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import com.Gig.Guide.GigGuide.Models.Event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Find all events for a specific club (legacy)
    List<Event> findByClub(Clubs club);

    // Optional: find upcoming events (legacy)
    List<Event> findByStartDateTimeAfter(LocalDateTime dateTime);

    // Paginated queries
    Page<Event> findByClubIdAndStatus(Long clubId, EventStatus status, Pageable pageable);

    Page<Event> findByStatusAndStartDateTimeAfter(EventStatus status, LocalDateTime now, Pageable pageable);

    Page<Event> findByClubIdAndStartDateTimeBetween(Long clubId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Event> findByClubId(Long clubId, Pageable pageable);

    Page<Event> findByClubIdAndStatusAndStartDateTimeBetween(Long clubId, EventStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Event> findByStatusAndStartDateTimeAfterAndClubId(EventStatus status, LocalDateTime now, Long clubId, Pageable pageable);

    Page<Event> findByStatusAndStartDateTimeBetween(EventStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
