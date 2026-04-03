package com.Gig.Guide.GigGuide.Models.Club;

import com.Gig.Guide.GigGuide.Models.BaseEntity;
import com.Gig.Guide.GigGuide.Models.Users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "clubs")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Clubs extends BaseEntity implements Serializable  {



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


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "social_id")
    private Socials socials;

    // Club owner (linked User account)
    @OneToOne
    @JoinColumn(name = "owner_user_id")
    private User owner;
}
