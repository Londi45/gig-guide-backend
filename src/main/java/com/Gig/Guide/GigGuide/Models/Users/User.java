package com.Gig.Guide.GigGuide.Models.Users;

import com.Gig.Guide.GigGuide.Enums.Role;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;      // For login

    @Column(nullable = false)
    private String password;      // Store hashed password!

    @Column(nullable = false, unique = true)
    private String email;

    private String fullName;

    private String phoneNumber;

    // User role: CLUB_OWNER, STAFF, CUSTOMER, ADMIN
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Optional: link staff/owner to a club
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Clubs club;

    private boolean isActive; // active or deactivated account

    // Email verification
    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;

    @Builder.Default
    private boolean isVerified = false;

    // Password reset
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;
}
