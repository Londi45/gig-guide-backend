package com.Gig.Guide.GigGuide.DTO;

import lombok.*;
import java.time.LocalDateTime;

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
    private String entryFee;
    private String ageRestriction;
    private String imageUrl;
    private boolean isActive;

    // Related club info
    private Long clubId;
    private String clubName;
}
