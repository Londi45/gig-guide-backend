package com.Gig.Guide.GigGuide.Models.Event;

import com.Gig.Guide.GigGuide.Models.BaseEntity;
import com.Gig.Guide.GigGuide.Models.Users.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "staff_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StaffAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
