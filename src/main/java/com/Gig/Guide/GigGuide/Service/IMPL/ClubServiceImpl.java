package com.Gig.Guide.GigGuide.Service.IMPL;

import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.Mapper.ClubMapper;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import com.Gig.Guide.GigGuide.Repositories.ClubRepository;
import com.Gig.Guide.GigGuide.Service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubServiceImpl implements ClubService {

    @Autowired
    private ClubRepository clubRepository;


    @Override
    public ClubDTO createClub(ClubDTO clubDTO) {

        Clubs clubs= ClubMapper.mapToEntity(clubDTO);
       Clubs clubs1 = clubRepository.save(clubs);
        return ClubMapper.mapToDTO(clubs1);
    }

    @Override
    public List<ClubDTO> getAllClubs() {
        return List.of();
    }

    @Override
    public ClubDTO getClubById(Long id) {
        return null;
    }

    @Override
    public ClubDTO updateClub(Long id, ClubDTO clubDTO) {
        return null;
    }

    @Override
    public void deleteClub(Long id) {

    }
}
