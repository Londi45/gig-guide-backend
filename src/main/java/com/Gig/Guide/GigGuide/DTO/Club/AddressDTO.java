package com.Gig.Guide.GigGuide.DTO.Club;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO implements Serializable {

    private String location;
    private String city;
    private String province;
    private String country;
    private String postalCode;
}


