package com.Gig.Guide.GigGuide.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
