package com.Gig.Guide.GigGuide.Service.IMPL;

import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Mapper.ClubMapper;
import com.Gig.Guide.GigGuide.Models.Club.Address;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import com.Gig.Guide.GigGuide.Models.Club.Socials;
import com.Gig.Guide.GigGuide.Models.Users.User;
import com.Gig.Guide.GigGuide.Repositories.AddressRepo;
import com.Gig.Guide.GigGuide.Repositories.ClubRepository;
import com.Gig.Guide.GigGuide.Repositories.SocialsRepo;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.ClubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ClubServiceImpl implements ClubService {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubMapper clubMapper;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private SocialsRepo socialsRepo;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ClubDTO createClub(ClubDTO clubDTO) {
        Clubs club = ClubMapper.mapToEntity(clubDTO);
        club.setActive(true);
        Clubs saved = clubRepository.save(club);
        log.info("Club added with id {} and name: {}", saved.getId(), saved.getName());
        return ClubMapper.mapToDTO(saved);
    }

    @Override
    public Page<ClubDTO> getAllClubs(Pageable pageable) {
        return clubRepository.findByActiveTrue(pageable)
                .map(ClubMapper::mapToDTO);
    }

    @Override
    public ClubDTO getClubById(Long id) {
        Clubs club = clubRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
        return ClubMapper.mapToDTO(club);
    }

    @Override
    public ClubDTO updateClub(Long id, ClubDTO clubDTO) {
        Clubs existingClub = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        existingClub.setName(clubDTO.getName());
        existingClub.setDescription(clubDTO.getDescription());
        existingClub.setEmail(clubDTO.getEmail());
        existingClub.setPhone(clubDTO.getPhone());
        existingClub.setWebsite(clubDTO.getWebsite());
        existingClub.setLogoUrl(clubDTO.getLogoUrl());
        existingClub.setCoverImageUrl(clubDTO.getCoverImageUrl());
        existingClub.setOpeningHours(clubDTO.getOpeningHours());
        existingClub.setClosingHours(clubDTO.getClosingHours());
        existingClub.setDressCode(clubDTO.getDressCode());
        existingClub.setHasParking(clubDTO.isHasParking());
        existingClub.setHasVIPArea(clubDTO.isHasVIPArea());
        existingClub.setCapacity(clubDTO.getCapacity());

        if (clubDTO.getAddress() != null) {
            if (existingClub.getAddress() == null) {
                Address address = new Address();
                address.setLocation(clubDTO.getAddress().getLocation());
                address.setCity(clubDTO.getAddress().getCity());
                address.setProvince(clubDTO.getAddress().getProvince());
                address.setCountry(clubDTO.getAddress().getCountry());
                address.setPostalCode(clubDTO.getAddress().getPostalCode());
                existingClub.setAddress(address);
            } else {
                Address address = existingClub.getAddress();
                address.setLocation(clubDTO.getAddress().getLocation());
                address.setCity(clubDTO.getAddress().getCity());
                address.setProvince(clubDTO.getAddress().getProvince());
                address.setCountry(clubDTO.getAddress().getCountry());
                address.setPostalCode(clubDTO.getAddress().getPostalCode());
            }
        }

        if (clubDTO.getSocials() != null) {
            if (existingClub.getSocials() == null) {
                Socials socials = new Socials();
                socials.setFacebookLink(clubDTO.getSocials().getFacebookLink());
                socials.setInstagramLink(clubDTO.getSocials().getInstagramLink());
                socials.setTwitterLink(clubDTO.getSocials().getTwitterLink());
                socials.setTiktokLink(clubDTO.getSocials().getTiktokLink());
                existingClub.setSocials(socials);
            } else {
                Socials socials = existingClub.getSocials();
                socials.setFacebookLink(clubDTO.getSocials().getFacebookLink());
                socials.setInstagramLink(clubDTO.getSocials().getInstagramLink());
                socials.setTwitterLink(clubDTO.getSocials().getTwitterLink());
                socials.setTiktokLink(clubDTO.getSocials().getTiktokLink());
            }
        }

        clubRepository.save(existingClub);
        return ClubMapper.mapToDTO(existingClub);
    }

    @Override
    public void deleteClub(Long id) {
        Clubs club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
        clubRepository.delete(club);
    }

    @Override
    public void deactivateClub(Long id) {
        Clubs club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
        club.setActive(false);
        clubRepository.save(club);

        // Deactivate all linked users
        List<User> users = userRepository.findByClubId(id);
        users.forEach(u -> u.setActive(false));
        userRepository.saveAll(users);
    }
}
