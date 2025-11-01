package com.Gig.Guide.GigGuide.DTO.Club;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDTO {


    private String fullName;
    private String email;
    private String phone;
}
