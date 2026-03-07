package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.Service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/clubs")
public class ClubController {

    @Autowired
    private ClubService clubService;

    @PostMapping("/create")
    @Operation(summary = "Creates a new club")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Club created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClubDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            )
    })
    public ResponseEntity<ClubDTO> createClub(@RequestBody ClubDTO clubDTO) {
        ClubDTO createdClub = clubService.createClub(clubDTO);
        log.info("Request to add club : {} " ,clubDTO.getName() );
        return new ResponseEntity<>(createdClub, HttpStatus.CREATED);
    }


    @Operation(summary = "List all clubs")
    @GetMapping
    public List<ClubDTO> getAllClubs() {
        log.info("Request received to get all clubs :");
        return clubService.getAllClubs();

    }

    @Operation(summary = "Gets club by Id  ")
    @GetMapping("/{id}")
    public ClubDTO getClubById(@PathVariable Long id) {
        return clubService.getClubById(id);
    }

    @Operation(summary = "Updates club Information ")
    @PutMapping("/{id}")
    public ClubDTO updateClub(@PathVariable Long id, @RequestBody ClubDTO clubDTO) {
        return clubService.updateClub(id, clubDTO);
    }

    @Operation(summary = "Deletes Club")
    @DeleteMapping("/{id}")
    public void deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
    }


}
