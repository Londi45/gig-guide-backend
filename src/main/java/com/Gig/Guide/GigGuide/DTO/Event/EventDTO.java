package com.Gig.Guide.GigGuide.DTO.Event;

import com.Gig.Guide.GigGuide.Enums.EventStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String genre;
    private String dressCode;
    private String ageRestriction;
    private String imageUrl;
    private EventStatus status;
    private boolean active;

    // Related club info
    private Long clubId;
    private String clubName;

    // Capacity and gender ratio
    private int capacity;
    private int maleRatio;
    private int femaleRatio;

    // Live attendance
    private int liveMaleCount;
    private int liveFemaleCount;
    private int liveTotalCount;
    private double liveMalePercentage;
    private double liveFemalePercentage;

    // Nested lists
    private List<EntryTypeDTO> entryTypes;
    private List<DiscountDTO> discounts;
}
