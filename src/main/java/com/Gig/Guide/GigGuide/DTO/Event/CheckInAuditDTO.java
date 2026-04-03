package com.Gig.Guide.GigGuide.DTO.Event;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInAuditDTO {

    private Long id;
    private Long eventId;
    private String gender;
    private String action;
    private Long performedBy;
    private LocalDateTime timestamp;
}
