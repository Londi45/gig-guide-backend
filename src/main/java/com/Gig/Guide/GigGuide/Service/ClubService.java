package com.Gig.Guide.GigGuide.Service;

import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;

import java.util.List;

public interface ClubService {
    ClubDTO createClub(ClubDTO clubDTO);
    List<ClubDTO> getAllClubs();
    ClubDTO getClubById(Long id);
    ClubDTO updateClub(Long id, ClubDTO clubDTO);
    void deleteClub(Long id);
}
