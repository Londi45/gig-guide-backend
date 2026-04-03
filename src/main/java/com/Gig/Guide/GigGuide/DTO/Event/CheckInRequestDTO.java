package com.Gig.Guide.GigGuide.DTO.Event;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInRequestDTO {

    @NotBlank
    private String gender; // "MALE" or "FEMALE"
}
