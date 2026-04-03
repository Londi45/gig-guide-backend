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
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        return ResponseEntity.ok(clubService.getAllClubs(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubDTO> getClubById(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getClubById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLUB_OWNER')")
    public ResponseEntity<ClubDTO> updateClub(@PathVariable Long id, @RequestBody ClubDTO clubDTO) {
        return ResponseEntity.ok(clubService.updateClub(id, clubDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateClub(@PathVariable Long id) {
        clubService.deactivateClub(id);
        return ResponseEntity.ok().build();
    }
}
