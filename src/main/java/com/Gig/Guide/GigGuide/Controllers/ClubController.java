package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.Service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/clubs")
public class ClubController {

    @Autowired
    private ClubService clubService;

    @PostMapping("create")
    public ResponseEntity<ClubDTO> createClub(@RequestBody ClubDTO clubDTO) {
        ClubDTO createdClub = clubService.createClub(clubDTO);
        return new ResponseEntity<>(createdClub, HttpStatus.CREATED);
    }


    @GetMapping
    public List<ClubDTO> getAllClubs() {
        return clubService.getAllClubs();
    }

    @GetMapping("/{id}")
    public ClubDTO getClubById(@PathVariable Long id) {
        return clubService.getClubById(id);
    }

    @PutMapping("/{id}")
    public ClubDTO updateClub(@PathVariable Long id, @RequestBody ClubDTO clubDTO) {
        return clubService.updateClub(id, clubDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
    }


}
