package com.Gig.Guide.GigGuide.DTO.Club;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialsDTO {


    private String facebookLink;
    private String instagramLink;
    private String twitterLink;
    private String tiktokLink;
}
