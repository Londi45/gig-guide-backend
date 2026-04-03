package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Models.Event.StaffAssignment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {

    List<StaffAssignment> findByEventId(Long eventId);

    @Transactional
    @Modifying
    void deleteByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}
