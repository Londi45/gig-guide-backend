package com.Gig.Guide.GigGuide.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role; // CLUB_OWNER, STAFF, CUSTOMER
    private Long clubId; // optional for staff
}
