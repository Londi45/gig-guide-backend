package com.Gig.Guide.GigGuide.Mapper;

import com.Gig.Guide.GigGuide.DTO.Club.AddressDTO;
import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.DTO.Club.SocialsDTO;
import com.Gig.Guide.GigGuide.Models.Club.Address;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import com.Gig.Guide.GigGuide.Models.Club.Socials;
import org.springframework.stereotype.Component;

@Component
public class ClubMapper {

    public static ClubDTO mapToDTO(Clubs club) {
        ClubDTO dto = new ClubDTO();
        dto.setId(club.getId());
        dto.setName(club.getName());
        dto.setDescription(club.getDescription());
        dto.setEmail(club.getEmail());
        dto.setPhone(club.getPhone());
        dto.setWebsite(club.getWebsite());
        dto.setLogoUrl(club.getLogoUrl());
        dto.setCoverImageUrl(club.getCoverImageUrl());
        dto.setOpeningHours(club.getOpeningHours());
        dto.setClosingHours(club.getClosingHours());
        dto.setDressCode(club.getDressCode());
        dto.setHasParking(club.isHasParking());
        dto.setHasVIPArea(club.isHasVIPArea());
        dto.setCapacity(club.getCapacity());
        dto.setActive(club.isActive());

        if (club.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setLocation(club.getAddress().getLocation());
            addressDTO.setCity(club.getAddress().getCity());
            addressDTO.setProvince(club.getAddress().getProvince());
            addressDTO.setCountry(club.getAddress().getCountry());
            addressDTO.setPostalCode(club.getAddress().getPostalCode());
            dto.setAddress(addressDTO);
        }

        if (club.getSocials() != null) {
            SocialsDTO socialsDTO = new SocialsDTO();
            socialsDTO.setFacebookLink(club.getSocials().getFacebookLink());
            socialsDTO.setInstagramLink(club.getSocials().getInstagramLink());
            socialsDTO.setTwitterLink(club.getSocials().getTwitterLink());
            socialsDTO.setTiktokLink(club.getSocials().getTiktokLink());
            dto.setSocials(socialsDTO);
        }

        return dto;
    }

    public static Clubs mapToEntity(ClubDTO dto) {
        Clubs club = new Clubs();
        club.setName(dto.getName());
        club.setDescription(dto.getDescription());
        club.setEmail(dto.getEmail());
        club.setPhone(dto.getPhone());
        club.setWebsite(dto.getWebsite());
        club.setLogoUrl(dto.getLogoUrl());
        club.setCoverImageUrl(dto.getCoverImageUrl());
        club.setOpeningHours(dto.getOpeningHours());
        club.setClosingHours(dto.getClosingHours());
        club.setDressCode(dto.getDressCode());
        club.setHasParking(dto.isHasParking());
        club.setHasVIPArea(dto.isHasVIPArea());
        club.setCapacity(dto.getCapacity());
        club.setActive(dto.isActive());

        if (dto.getAddress() != null) {
            Address address = new Address();
            address.setLocation(dto.getAddress().getLocation());
            address.setCity(dto.getAddress().getCity());
            address.setProvince(dto.getAddress().getProvince());
            address.setCountry(dto.getAddress().getCountry());
            address.setPostalCode(dto.getAddress().getPostalCode());
            club.setAddress(address);
        }

        if (dto.getSocials() != null) {
            Socials socials = new Socials();
            socials.setFacebookLink(dto.getSocials().getFacebookLink());
            socials.setInstagramLink(dto.getSocials().getInstagramLink());
            socials.setTwitterLink(dto.getSocials().getTwitterLink());
            socials.setTiktokLink(dto.getSocials().getTiktokLink());
            club.setSocials(socials);
        }

        return club;
    }
}
