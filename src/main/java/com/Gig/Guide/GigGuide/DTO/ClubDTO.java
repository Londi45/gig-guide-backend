package com.Gig.Guide.GigGuide.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubDTO {

    private Long id;
    private String name;
    private String description;
    private String email;
    private String phone;
    private String website;
    private int capacity;
    private String openingHours;
    private String dressCode;
    private boolean hasParking;
    private boolean hasVIPArea;
    private String logoUrl;
    private String coverImageUrl;

    // Address fields
    private String location;
    private String city;
    private String province;
    private String country;
    private String postalCode;
}
