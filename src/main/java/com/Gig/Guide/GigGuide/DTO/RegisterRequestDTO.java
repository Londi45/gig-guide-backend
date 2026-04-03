package com.Gig.Guide.GigGuide.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String fullName;

    private String phoneNumber;

    @NotBlank
    private String role; // CLUB_OWNER or STAFF — validated in service layer

    private Long clubId; // required when role is STAFF
}
