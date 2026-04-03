package com.Gig.Guide.GigGuide.Models.Event;

import com.Gig.Guide.GigGuide.Models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Discount extends BaseEntity {

    @Column(nullable = false)
    private String discountType;

    @Column(nullable = false)
    private BigDecimal discountValue;

    private String description;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
