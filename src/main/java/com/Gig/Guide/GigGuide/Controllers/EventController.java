package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Event.EventDTO;
import com.Gig.Guide.GigGuide.DTO.Event.StatusTransitionRequestDTO;
import com.Gig.Guide.GigGuide.Enums.EventStatus;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("GET /api/events - page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").ascending());
        Page<EventDTO> result = eventService.getPublishedEvents(pageable);
        log.info("Fetched {} published events (total={})", result.getNumberOfElements(), result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        log.info("GET /api/events/{}", id);
        EventDTO event = eventService.getEventById(id);
        log.info("Fetched event - id={}, name={}", event.getId(), event.getName());
        return ResponseEntity.ok(event);
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<Page<EventDTO>> getEventsByClub(
            @PathVariable String clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/events/club/{} - page={}, size={}", clubId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").ascending());
        Page<EventDTO> result = eventService.getEventsByClub(clubId, pageable);
        log.info("Fetched {} events for clubId={} (total={})", result.getNumberOfElements(), clubId, result.getTotalElements());
        return ResponseEntity.ok(result);
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
        log.info("GET /api/events/dashboard - userId={}, status={}, startDate={}, endDate={}", userId, status, startDate, endDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").ascending());
        Page<EventDTO> result = eventService.getDashboardEvents(userId, status, startDate, endDate, pageable);
        log.info("Dashboard fetched {} events for userId={}", result.getNumberOfElements(), userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EventDTO> createEvent(@RequestBody EventDTO dto, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("POST /api/events - userId={}, eventName={}, clubId={}", userId, dto.getName(), dto.getClubId());
        EventDTO created = eventService.createEvent(dto, userId);
        log.info("Event created - id={}, name={}, clubId={}", created.getId(), created.getName(), created.getClubId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id, @RequestBody EventDTO dto, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("PUT /api/events/{} - userId={}", id, userId);
        EventDTO updated = eventService.updateEvent(id, dto, userId);
        log.info("Event updated - id={}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("DELETE /api/events/{} - userId={}", id, userId);
        eventService.deleteEvent(id, userId);
        log.info("Event deleted - id={}", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EventDTO> transitionStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusTransitionRequestDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("PATCH /api/events/{}/status - userId={}, newStatus={}", id, userId, dto.getStatus());
        EventDTO updated = eventService.transitionStatus(id, dto.getStatus(), userId);
        log.info("Event status updated - id={}, status={}", id, updated.getStatus());
        return ResponseEntity.ok(updated);
    }
}
