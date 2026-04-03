package com.Gig.Guide.GigGuide.DTO.Event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountDTO {

    private Long id;
    private String discountType;
    private BigDecimal discountValue;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}
