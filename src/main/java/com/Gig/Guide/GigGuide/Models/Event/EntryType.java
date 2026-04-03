package com.Gig.Guide.GigGuide.Models.Event;

import com.Gig.Guide.GigGuide.Models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "entry_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EntryType extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private String description;

    private int availableQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
