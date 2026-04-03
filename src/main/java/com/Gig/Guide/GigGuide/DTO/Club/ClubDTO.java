package com.Gig.Guide.GigGuide.DTO.Club;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubDTO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private String email;
    private String phone;
    private String website;
    private String logoUrl;
    private String coverImageUrl;
    private String openingHours;
    private String closingHours;
    private String dressCode;
    private boolean hasParking;
    private boolean hasVIPArea;
    private int capacity;
    private boolean active; // isActive — using 'active' to avoid Lombok boolean naming issues
    private AddressDTO address;
    private SocialsDTO socials;
}
