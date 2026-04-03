package com.Gig.Guide.GigGuide.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String role;
}
