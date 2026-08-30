package com.Gig.Guide.GigGuide.Models.Club;

import com.Gig.Guide.GigGuide.Models.BaseEntity;
import com.Gig.Guide.GigGuide.Models.Users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "clubs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clubs implements Serializable {

    @Id
    @UuidGenerator                          // Hibernate generates a UUID before INSERT
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

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

    @Column(name = "hasviparea")
    private boolean hasVIPArea;
    private int capacity;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "social_id")
    private Socials socials;

    @OneToOne
    @JoinColumn(name = "owner_user_id")
    private User owner;
}
