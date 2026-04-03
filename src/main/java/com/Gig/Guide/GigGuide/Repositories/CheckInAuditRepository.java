package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Models.Event.CheckInAuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInAuditRepository extends JpaRepository<CheckInAuditEntry, Long> {

    Page<CheckInAuditEntry> findByEventIdOrderByTimestampDesc(Long eventId, Pageable pageable);
}
