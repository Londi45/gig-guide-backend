package com.Gig.Guide.GigGuide.Models.Club;

import com.Gig.Guide.GigGuide.Models.BaseEntity;
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
    private String dressCode;
    private boolean hasParking;
    private boolean hasVIPArea;
    private boolean isActive;
    private int capacity;

    // Address relationship
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    // Socials relationship
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "social_id")
    private Socials socials;

    // Owner relationship (many clubs can belong to one owner)
//    @ManyToOne
//    @JoinColumn(name = "owner_id")
//    private Owner owner;
}
