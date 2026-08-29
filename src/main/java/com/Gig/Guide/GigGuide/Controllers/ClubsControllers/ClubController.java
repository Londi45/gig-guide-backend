package com.Gig.Guide.GigGuide.Controllers.ClubsControllers;

import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.Service.ClubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/clubs")
public class ClubController {

    @Autowired
    private ClubService clubService;

    @GetMapping
    public ResponseEntity<Page<ClubDTO>> getAllClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {
        log.info("GET /api/clubs - page={}, size={}, sort={}", page, size, sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<ClubDTO> result = clubService.getAllClubs(pageable);
        log.info("Fetched {} clubs (total={})", result.getNumberOfElements(), result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubDTO> getClubById(@PathVariable Long id) {
        log.info("GET /api/clubs/{}", id);
        ClubDTO club = clubService.getClubById(id);
        log.info("Fetched club - id={}, name={}", club.getId(), club.getName());
        return ResponseEntity.ok(club);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLUB_OWNER')")
    public ResponseEntity<ClubDTO> updateClub(@PathVariable Long id, @RequestBody ClubDTO clubDTO) {
        log.info("PUT /api/clubs/{} - name={}", id, clubDTO.getName());
        ClubDTO updated = clubService.updateClub(id, clubDTO);
        log.info("Club updated - id={}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        log.info("DELETE /api/clubs/{}", id);
        clubService.deleteClub(id);
        log.info("Club deleted - id={}", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateClub(@PathVariable Long id) {
        log.info("PATCH /api/clubs/{}/deactivate", id);
        clubService.deactivateClub(id);
        log.info("Club deactivated - id={}", id);
        return ResponseEntity.ok().build();
    }
}
