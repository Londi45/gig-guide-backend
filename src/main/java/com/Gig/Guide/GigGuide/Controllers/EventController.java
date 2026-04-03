package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Event.EventDTO;
import com.Gig.Guide.GigGuide.DTO.Event.StatusTransitionRequestDTO;
import com.Gig.Guide.GigGuide.Enums.EventStatus;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
@CrossOrigin("*")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @GetMapping
    public ResponseEntity<Page<EventDTO>> getPublishedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").ascending());
        return ResponseEntity.ok(eventService.getPublishedEvents(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<Page<EventDTO>> getEventsByClub(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").ascending());
        return ResponseEntity.ok(eventService.getEventsByClub(clubId, pageable));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF')")
    public ResponseEntity<Page<EventDTO>> getDashboardEvents(
            Authentication authentication,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").ascending());
        return ResponseEntity.ok(eventService.getDashboardEvents(userId, status, startDate, endDate, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EventDTO> createEvent(@RequestBody EventDTO dto, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(dto, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id, @RequestBody EventDTO dto, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(eventService.updateEvent(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        eventService.deleteEvent(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EventDTO> transitionStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusTransitionRequestDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(eventService.transitionStatus(id, dto.getStatus(), userId));
    }
}
