package com.Gig.Guide.GigGuide.Service.IMPL;

import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.Mapper.ClubMapper;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import com.Gig.Guide.GigGuide.Repositories.ClubRepository;
import com.Gig.Guide.GigGuide.Service.ClubService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.EvictEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClubServiceImpl implements ClubService {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubMapper clubMapper;


    @Override
    public ClubDTO createClub(ClubDTO clubDTO) {

        Clubs clubs= ClubMapper.mapToEntity(clubDTO);
       Clubs clubs1 = clubRepository.save(clubs);
       log.info("Club added with id {} and name : {}",clubs1.getId(),clubs1.getName());
        return ClubMapper.mapToDTO(clubs1);
    }

    @Cacheable(value = "clubs")
    @Override
    public List<ClubDTO> getAllClubs() {
        List<Clubs> clubsList = clubRepository.findAll();
        return clubsList.stream()
                .map(ClubMapper::mapToDTO)
                .collect(Collectors.toList());
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
