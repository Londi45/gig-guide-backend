package com.Gig.Guide.GigGuide.Models.Club;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "socials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Socials implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String facebookLink;
    private String instagramLink;
    private String twitterLink;
    private String tiktokLink;

}