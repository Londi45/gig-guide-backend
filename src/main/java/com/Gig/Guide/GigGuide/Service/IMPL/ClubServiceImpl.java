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
import com.Gig.Guide.GigGuide.Kafka.events.ClubCreatedEvent;
import com.Gig.Guide.GigGuide.Kafka.producer.ClubEventProducer;
import com.Gig.Guide.GigGuide.Service.ClubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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

    @Autowired
    private ClubEventProducer clubEventProducer;

    @Override
    public ClubDTO createClub(ClubDTO clubDTO) {
        log.info("Creating club - name={}, email={}", clubDTO.getName(), clubDTO.getEmail());

        if (clubDTO.getName() == null || clubDTO.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Club name is required");
        }

        Clubs club = ClubMapper.mapToEntity(clubDTO);
        club.setActive(true);
        Clubs saved = clubRepository.save(club);
        log.info("Club created - id={}, name={}", saved.getId(), saved.getName());

        // Publish event to Kafka asynchronously — does not block the HTTP response
        ClubCreatedEvent event = ClubCreatedEvent.builder()
                .clubId(saved.getId())
                .clubName(saved.getName())
                .email(saved.getEmail())
                .city(saved.getAddress() != null ? saved.getAddress().getCity() : null)
                .createdAt(LocalDateTime.now())
                .build();
        clubEventProducer.publishClubCreated(event);

        return ClubMapper.mapToDTO(saved);
    }

    @Override
    public Page<ClubDTO> getAllClubs(Pageable pageable) {
        log.info("Fetching all active clubs - page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ClubDTO> result = clubRepository.findByActiveTrue(pageable).map(ClubMapper::mapToDTO);
        log.info("Fetched {} clubs (total={})", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    @Override
    public ClubDTO getClubById(String id) {
        log.info("Fetching club - id={}", id);
        Clubs club = clubRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
        log.info("Club found - id={}, name={}", club.getId(), club.getName());
        return ClubMapper.mapToDTO(club);
    }

    @Override
    public ClubDTO updateClub(String id, ClubDTO clubDTO) {
        log.info("Updating club - id={}", id);
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
        log.info("Club updated - id={}", id);
        return ClubMapper.mapToDTO(existingClub);
    }

    @Override
    public void deleteClub(String id) {
        log.info("Deleting club - id={}", id);
        Clubs club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
        clubRepository.delete(club);
        log.info("Club deleted - id={}", id);
    }

    @Override
    public void deactivateClub(String id) {
        log.info("Deactivating club - id={}", id);
        Clubs club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
        club.setActive(false);
        clubRepository.save(club);

        // Deactivate all linked users
        List<User> users = userRepository.findByClubId(id);
        users.forEach(u -> u.setActive(false));
        userRepository.saveAll(users);
        log.info("Club and {} linked users deactivated - clubId={}", users.size(), id);
    }
}
