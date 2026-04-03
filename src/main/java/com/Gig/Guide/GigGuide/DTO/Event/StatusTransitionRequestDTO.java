package com.Gig.Guide.GigGuide.DTO.Event;

import com.Gig.Guide.GigGuide.Enums.EventStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTransitionRequestDTO {

    @NotNull
    private EventStatus status;
}
