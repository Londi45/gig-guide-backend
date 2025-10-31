package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Models.Club.Clubs;

import com.Gig.Guide.GigGuide.Models.Event.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Find all events for a specific club
    List<Event> findByClub(Clubs club);

    // Optional: find upcoming events
    List<Event> findByStartDateTimeAfter(LocalDateTime dateTime);
}
