package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Event.EntryTypeDTO;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/events/{eventId}/entry-types")
@CrossOrigin("*")
public class EntryTypeController {

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
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EntryTypeDTO> createEntryType(
            @PathVariable Long eventId,
            @RequestBody EntryTypeDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("POST /api/events/{}/entry-types - userId={}, name={}, price={}", eventId, userId, dto.getName(), dto.getPrice());
        EntryTypeDTO created = eventService.createEntryType(eventId, dto, userId);
        log.info("Entry type created - id={}, eventId={}", created.getId(), eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{entryTypeId}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EntryTypeDTO> updateEntryType(
            @PathVariable Long eventId,
            @PathVariable Long entryTypeId,
            @RequestBody EntryTypeDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("PUT /api/events/{}/entry-types/{} - userId={}", eventId, entryTypeId, userId);
        EntryTypeDTO updated = eventService.updateEntryType(entryTypeId, dto, userId);
        log.info("Entry type updated - id={}", entryTypeId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{entryTypeId}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<Void> deleteEntryType(
            @PathVariable Long eventId,
            @PathVariable Long entryTypeId,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("DELETE /api/events/{}/entry-types/{} - userId={}", eventId, entryTypeId, userId);
        eventService.deleteEntryType(entryTypeId, userId);
        log.info("Entry type deleted - id={}", entryTypeId);
        return ResponseEntity.noContent().build();
    }
}
