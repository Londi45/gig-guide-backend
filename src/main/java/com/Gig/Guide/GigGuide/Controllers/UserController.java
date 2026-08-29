package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.RegisterRequestDTO;
import com.Gig.Guide.GigGuide.DTO.UpdateProfileDTO;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @PostMapping("/staff")
    @PreAuthorize("hasRole('CLUB_OWNER')")
    public ResponseEntity<UserResponseDTO> createStaff(
            @Valid @RequestBody RegisterRequestDTO dto,
            Authentication authentication) {
        Long requesterId = getCurrentUserId(authentication);
        log.info("POST /api/users/staff - requesterId={}, newStaffEmail={}", requesterId, dto.getEmail());
        UserResponseDTO response = userService.createStaff(dto, requesterId);
        log.info("Staff created successfully - staffId={}, email={}", response.getId(), response.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/staff/{userId}/deactivate")
    @PreAuthorize("hasRole('CLUB_OWNER')")
    public ResponseEntity<Void> deactivateStaff(
            @PathVariable Long userId,
            Authentication authentication) {
        Long requesterId = getCurrentUserId(authentication);
        log.info("PATCH /api/users/staff/{}/deactivate - requesterId={}", userId, requesterId);
        userService.deactivateStaff(userId, requesterId);
        log.info("Staff deactivated - staffId={}", userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF')")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @RequestBody UpdateProfileDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("PUT /api/users/profile - userId={}", userId);
        UserResponseDTO response = userService.updateProfile(userId, dto);
        log.info("Profile updated - userId={}", userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF')")
    public ResponseEntity<List<UserResponseDTO>> getStaff(Authentication authentication) {
        Long requesterId = getCurrentUserId(authentication);
        log.info("GET /api/users/staff - requesterId={}", requesterId);
        List<UserResponseDTO> staff = userService.getStaffByClub(requesterId);
        log.info("Fetched {} staff members for requesterId={}", staff.size(), requesterId);
        return ResponseEntity.ok(staff);
    }
}
