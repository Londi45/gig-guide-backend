package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Event.EventDTO;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/image")
@CrossOrigin("*")
public class ImageController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EventDTO> uploadImage(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(eventService.uploadImage(eventId, file, userId));
    }

    @PutMapping("/url")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<EventDTO> setImageUrl(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        String url = body.get("url");
        return ResponseEntity.ok(eventService.setImageUrl(eventId, url, userId));
    }
}
