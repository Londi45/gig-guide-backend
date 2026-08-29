package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Event.CheckInAuditDTO;
import com.Gig.Guide.GigGuide.DTO.Event.CheckInRequestDTO;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/events/{eventId}/attendance")
@CrossOrigin("*")
public class AttendanceController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF')")
    public ResponseEntity<Map<String, Object>> checkIn(
            @PathVariable Long eventId,
            @Valid @RequestBody CheckInRequestDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("Check-in recorded event {} - userId={}, gender={}", eventId, userId, dto.getGender());
        Map<String, Object> result = eventService.checkIn(eventId, dto.getGender(), userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF')")
    public ResponseEntity<Map<String, Object>> checkOut(
            @PathVariable Long eventId,
            @Valid @RequestBody CheckInRequestDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("Check-out recorded - eventId={} - userId={}, gender={}", eventId, userId, dto.getGender());
        Map<String, Object> result = eventService.checkOut(eventId, dto.getGender(), userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/audit")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'ADMIN')")
    public ResponseEntity<Page<CheckInAuditDTO>> getAuditLog(
            @PathVariable Long eventId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<CheckInAuditDTO> result = eventService.getAuditLog(eventId, userId, pageable);
        log.info("Audit log fetched - eventId={}, entries={}", eventId, result.getNumberOfElements());
        return ResponseEntity.ok(result);
    }
}
