package com.Gig.Guide.GigGuide.DTO.Event;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryTypeDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private int availableQuantity;
}
