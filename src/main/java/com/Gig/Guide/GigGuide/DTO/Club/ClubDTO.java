package com.Gig.Guide.GigGuide.DTO.Club;

import com.Gig.Guide.GigGuide.Models.Club.Socials;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubDTO {


    private String name;
    private String description;
    private String email;
    private String phone;
    private String website;
    private String logoUrl;
    private String coverImageUrl;
    private String openingHours;
    private String dressCode;
    private boolean hasParking;
    private boolean hasVIPArea;
    private int capacity;
    private AddressDTO addressDTO;
    private SocialsDTO socialsdto;
//    private OwnerDTO ownerDTO;
}
