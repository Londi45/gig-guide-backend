package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.RegisterRequestDTO;
import com.Gig.Guide.GigGuide.DTO.UpdateProfileDTO;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createStaff(dto, requesterId));
    }

    @PatchMapping("/staff/{userId}/deactivate")
    @PreAuthorize("hasRole('CLUB_OWNER')")
    public ResponseEntity<Void> deactivateStaff(
            @PathVariable Long userId,
            Authentication authentication) {
        Long requesterId = getCurrentUserId(authentication);
        userService.deactivateStaff(userId, requesterId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF')")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @RequestBody UpdateProfileDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(userService.updateProfile(userId, dto));
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF')")
    public ResponseEntity<List<UserResponseDTO>> getStaff(Authentication authentication) {
        Long requesterId = getCurrentUserId(authentication);
        return ResponseEntity.ok(userService.getStaffByClub(requesterId));
    }
}
