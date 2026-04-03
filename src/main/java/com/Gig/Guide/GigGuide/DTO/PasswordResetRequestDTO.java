package com.Gig.Guide.GigGuide.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetRequestDTO {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
