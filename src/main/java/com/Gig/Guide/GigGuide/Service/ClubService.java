package com.Gig.Guide.GigGuide.Service;

import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClubService {

    ClubDTO createClub(ClubDTO clubDTO);

    Page<ClubDTO> getAllClubs(Pageable pageable);

    ClubDTO getClubById(Long id);

    ClubDTO updateClub(Long id, ClubDTO clubDTO);

    void deleteClub(Long id);

    void deactivateClub(Long id);
}
