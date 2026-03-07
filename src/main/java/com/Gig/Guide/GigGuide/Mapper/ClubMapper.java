package com.Gig.Guide.GigGuide.Mapper;


import com.Gig.Guide.GigGuide.DTO.Club.AddressDTO;
import com.Gig.Guide.GigGuide.DTO.Club.ClubDTO;
import com.Gig.Guide.GigGuide.DTO.Club.OwnerDTO;
import com.Gig.Guide.GigGuide.DTO.Club.SocialsDTO;
import com.Gig.Guide.GigGuide.Models.Club.Address;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import com.Gig.Guide.GigGuide.Models.Club.Owner;
import com.Gig.Guide.GigGuide.Models.Club.Socials;
import org.springframework.stereotype.Component;

@Component
public class ClubMapper {

    public static ClubDTO mapToDTO(Clubs club) {
        ClubDTO dto = new ClubDTO();

        dto.setName(club.getName());
        dto.setDescription(club.getDescription());
        dto.setEmail(club.getEmail());
        dto.setPhone(club.getPhone());
        dto.setWebsite(club.getWebsite());
        dto.setLogoUrl(club.getLogoUrl());
        dto.setCoverImageUrl(club.getCoverImageUrl());
        dto.setOpeningHours(club.getOpeningHours());
        dto.setDressCode(club.getDressCode());
        dto.setHasParking(club.isHasParking());
        dto.setHasVIPArea(club.isHasVIPArea());
        dto.setCapacity(club.getCapacity());


        if (club.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setLocation(club.getAddress().getLocation());
            addressDTO.setCity(club.getAddress().getCity());
            addressDTO.setProvince(club.getAddress().getProvince());
            addressDTO.setCountry(club.getAddress().getCountry());
            addressDTO.setPostalCode(club.getAddress().getPostalCode());
            dto.setAddressDTO(addressDTO);
        }


        if (club.getSocials() != null) {
            SocialsDTO socialsDTO = new SocialsDTO();
            socialsDTO.setFacebookLink(club.getSocials().getFacebookLink());
            socialsDTO.setInstagramLink(club.getSocials().getInstagramLink());
            socialsDTO.setTwitterLink(club.getSocials().getTwitterLink());
            socialsDTO.setTiktokLink(club.getSocials().getTiktokLink());
            dto.setSocialsdto(socialsDTO);
        }


//        if (club.getOwner() != null) {
//            OwnerDTO ownerDTO = new OwnerDTO();
//            ownerDTO.setFullName(club.getOwner().getFullName());
//            ownerDTO.setEmail(club.getOwner().getEmail());
//            ownerDTO.setPhone(club.getOwner().getPhone());
//            dto.setOwnerDTO(ownerDTO);
//        }

        return dto;
    }


    // Convert DTO → Entity
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
        club.setDressCode(dto.getDressCode());
        club.setHasParking(dto.isHasParking());
        club.setHasVIPArea(dto.isHasVIPArea());
        club.setCapacity(dto.getCapacity());

        // Address
        if (dto.getAddressDTO() != null) {
            Address address = new Address();
            address.setLocation(dto.getAddressDTO().getLocation());
            address.setCity(dto.getAddressDTO().getCity());
            address.setProvince(dto.getAddressDTO().getProvince());
            address.setCountry(dto.getAddressDTO().getCountry());
            address.setPostalCode(dto.getAddressDTO().getPostalCode());
            club.setAddress(address);
        }

        // Socials
        if (dto.getSocialsdto() != null) {
            Socials socials = new Socials();
            socials.setFacebookLink(dto.getSocialsdto().getFacebookLink());
            socials.setInstagramLink(dto.getSocialsdto().getInstagramLink());
            socials.setTwitterLink(dto.getSocialsdto().getTwitterLink());
            socials.setTiktokLink(dto.getSocialsdto().getTiktokLink());
            club.setSocials(socials);
        }

        // Owner
//        if (dto.getOwnerDTO() != null) {
//            Owner owner = new Owner();
//            owner.setFullName(dto.getOwnerDTO().getFullName());
//            owner.setEmail(dto.getOwnerDTO().getEmail());
//            owner.setPhone(dto.getOwnerDTO().getPhone());
//            club.setOwner(owner);
//        }

        return club;
    }

}
