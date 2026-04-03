package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/staff")
@CrossOrigin("*")
public class StaffAssignmentController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('CLUB_OWNER')")
    public ResponseEntity<Void> assignStaff(
            @PathVariable Long eventId,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        Long requesterId = getCurrentUserId(authentication);
        Long staffUserId = body.get("staffUserId");
        eventService.assignStaff(eventId, staffUserId, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('CLUB_OWNER')")
    public ResponseEntity<Void> removeStaffAssignment(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            Authentication authentication) {
        Long requesterId = getCurrentUserId(authentication);
        eventService.removeStaffAssignment(eventId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF')")
    public ResponseEntity<List<UserResponseDTO>> getEventStaff(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventStaff(eventId));
    }
}
