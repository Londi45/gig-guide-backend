package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Models.Event.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntryTypeRepository extends JpaRepository<EntryType, Long> {

    List<EntryType> findByEventId(Long eventId);
}
