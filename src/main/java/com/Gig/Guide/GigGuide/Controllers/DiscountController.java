package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Event.DiscountDTO;
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
        log.info("POST /api/events/{}/discounts - userId={}, discountType={}", eventId, userId, dto.getDiscountType());
        DiscountDTO created = eventService.createDiscount(eventId, dto, userId);
        log.info("Discount created - id={}, eventId={}", created.getId(), eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{discountId}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<DiscountDTO> updateDiscount(
            @PathVariable Long eventId,
            @PathVariable Long discountId,
            @RequestBody DiscountDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("PUT /api/events/{}/discounts/{} - userId={}", eventId, discountId, userId);
        DiscountDTO updated = eventService.updateDiscount(discountId, dto, userId);
        log.info("Discount updated - id={}", discountId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{discountId}")
    @PreAuthorize("hasAnyRole('CLUB_OWNER', 'STAFF', 'ADMIN')")
    public ResponseEntity<Void> deleteDiscount(
            @PathVariable Long eventId,
            @PathVariable Long discountId,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        log.info("DELETE /api/events/{}/discounts/{} - userId={}", eventId, discountId, userId);
        eventService.deleteDiscount(discountId, userId);
        log.info("Discount deleted - id={}", discountId);
        return ResponseEntity.noContent().build();
    }
}
