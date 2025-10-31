package com.Gig.Guide.GigGuide.Models.Event;

import com.Gig.Guide.GigGuide.Models.BaseEntity;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity implements Serializable {



    // Basic info
    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    private String genre;         // e.g. "House, Hip-Hop, Jazz"
    private String dressCode;     // Optional event-specific dress code
    private String entryFee;      // e.g. "R150"
    private String ageRestriction; // e.g. "18+ only"

    private String imageUrl;      // Event banner or poster


    // Many events can belong to one club
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Clubs club;
}
