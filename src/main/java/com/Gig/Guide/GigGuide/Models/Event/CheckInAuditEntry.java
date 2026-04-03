package com.Gig.Guide.GigGuide.Models.Event;

import com.Gig.Guide.GigGuide.Models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "check_in_audit_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CheckInAuditEntry extends BaseEntity {

    private String gender;

    private String action; // CHECK_IN or CHECK_OUT

    private Long performedBy;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
