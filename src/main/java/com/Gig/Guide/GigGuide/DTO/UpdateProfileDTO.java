package com.Gig.Guide.GigGuide.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileDTO {

    private String fullName;
    private String phoneNumber;
}
