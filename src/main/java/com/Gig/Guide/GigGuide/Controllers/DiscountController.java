package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Event.DiscountDTO;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events/{eventId}/discounts")
@CrossOrigin("*")
public class DiscountController {

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
    public ResponseEntity<DiscountDTO> createDiscount(
            @PathVariable Long eventId,
            @RequestBody DiscountDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createDiscount(eventId, dto, userId));
    }

    @PutMapping("/{discountId}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<DiscountDTO> updateDiscount(
            @PathVariable Long eventId,
            @PathVariable Long discountId,
            @RequestBody DiscountDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(eventService.updateDiscount(discountId, dto, userId));
    }

    @DeleteMapping("/{discountId}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<Void> deleteDiscount(
            @PathVariable Long eventId,
            @PathVariable Long discountId,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        eventService.deleteDiscount(discountId, userId);
        return ResponseEntity.noContent().build();
    }
}
