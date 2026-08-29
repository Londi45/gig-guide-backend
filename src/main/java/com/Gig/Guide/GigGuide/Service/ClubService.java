package com.Gig.Guide.GigGuide.Service;

import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClubService {

    ClubDTO createClub(ClubDTO clubDTO);

    Page<ClubDTO> getAllClubs(Pageable pageable);

    ClubDTO getClubById(String id);

    ClubDTO updateClub(String id, ClubDTO clubDTO);

    void deleteClub(String id);

    void deactivateClub(String id);
}
