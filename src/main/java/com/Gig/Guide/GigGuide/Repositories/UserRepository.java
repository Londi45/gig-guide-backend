package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Enums.Role;
import com.Gig.Guide.GigGuide.Models.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username for login
    Optional<User> findByUsername(String username);

    // Check if username already exists
    boolean existsByUsername(String username);

    // Check if email already exists
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    List<User> findByClubId(Long clubId);

    List<User> findByClubIdAndRole(Long clubId, Role role);
}
