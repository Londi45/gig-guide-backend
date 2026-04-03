package com.Gig.Guide.GigGuide.Models.Event;

import com.Gig.Guide.GigGuide.Enums.EventStatus;
import com.Gig.Guide.GigGuide.Models.BaseEntity;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Event extends BaseEntity implements Serializable {

    // Basic info
    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    private String genre;
    private String dressCode;
    private String ageRestriction;
    private String imageUrl;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    // Capacity and gender ratio
    private int capacity;

    @Builder.Default
    private int maleRatio = 50;

    @Builder.Default
    private int femaleRatio = 50;

    // Live attendance counts
    @Builder.Default
    private int liveMaleCount = 0;

    @Builder.Default
    private int liveFemaleCount = 0;

    // Many events can belong to one club
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Clubs club;

    // Entry types for this event
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EntryType> entryTypes = new ArrayList<>();

    // Discounts for this event
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Discount> discounts = new ArrayList<>();
}
