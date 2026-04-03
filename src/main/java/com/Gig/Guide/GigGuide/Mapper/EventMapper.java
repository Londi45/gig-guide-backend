package com.Gig.Guide.GigGuide.Mapper;

import com.Gig.Guide.GigGuide.DTO.Event.CheckInAuditDTO;
import com.Gig.Guide.GigGuide.DTO.Event.DiscountDTO;
import com.Gig.Guide.GigGuide.DTO.Event.EntryTypeDTO;
import com.Gig.Guide.GigGuide.DTO.Event.EventDTO;
import com.Gig.Guide.GigGuide.Models.Event.CheckInAuditEntry;
import com.Gig.Guide.GigGuide.Models.Event.Discount;
import com.Gig.Guide.GigGuide.Models.Event.EntryType;
import com.Gig.Guide.GigGuide.Models.Event.Event;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    public static EventDTO toDTO(Event event) {
        return toDTO(event, false);
    }

    /**
     * @param activeDiscountsOnly if true, only include currently active discounts (for public endpoints)
     */
    public static EventDTO toDTO(Event event, boolean activeDiscountsOnly) {
        int maleCount = event.getLiveMaleCount();
        int femaleCount = event.getLiveFemaleCount();
        int total = maleCount + femaleCount;

        double malePercentage = total > 0 ? Math.round(maleCount * 100.0 / total * 10.0) / 10.0 : 0.0;
        double femalePercentage = total > 0 ? Math.round(femaleCount * 100.0 / total * 10.0) / 10.0 : 0.0;

        List<EntryTypeDTO> entryTypeDTOs = event.getEntryTypes() == null ? List.of() :
                event.getEntryTypes().stream().map(EventMapper::toEntryTypeDTO).collect(Collectors.toList());

        List<DiscountDTO> discountDTOs;
        if (event.getDiscounts() == null) {
            discountDTOs = List.of();
        } else if (activeDiscountsOnly) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            discountDTOs = event.getDiscounts().stream()
                    .filter(d -> d.getValidFrom() != null && d.getValidUntil() != null
                            && !now.isBefore(d.getValidFrom()) && !now.isAfter(d.getValidUntil()))
                    .map(EventMapper::toDiscountDTO)
                    .collect(Collectors.toList());
        } else {
            discountDTOs = event.getDiscounts().stream().map(EventMapper::toDiscountDTO).collect(Collectors.toList());
        }

        return EventDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .genre(event.getGenre())
                .dressCode(event.getDressCode())
                .ageRestriction(event.getAgeRestriction())
                .imageUrl(event.getImageUrl())
                .status(event.getStatus())
                .active(event.isActive())
                .clubId(event.getClub() != null ? event.getClub().getId() : null)
                .clubName(event.getClub() != null ? event.getClub().getName() : null)
                .capacity(event.getCapacity())
                .maleRatio(event.getMaleRatio())
                .femaleRatio(event.getFemaleRatio())
                .liveMaleCount(maleCount)
                .liveFemaleCount(femaleCount)
                .liveTotalCount(total)
                .liveMalePercentage(malePercentage)
                .liveFemalePercentage(femalePercentage)
                .entryTypes(entryTypeDTOs)
                .discounts(discountDTOs)
                .build();
    }

    public static EntryTypeDTO toEntryTypeDTO(EntryType entryType) {
        return EntryTypeDTO.builder()
                .id(entryType.getId())
                .name(entryType.getName())
                .price(entryType.getPrice())
                .description(entryType.getDescription())
                .availableQuantity(entryType.getAvailableQuantity())
                .build();
    }

    public static DiscountDTO toDiscountDTO(Discount discount) {
        return DiscountDTO.builder()
                .id(discount.getId())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getDiscountValue())
                .description(discount.getDescription())
                .validFrom(discount.getValidFrom())
                .validUntil(discount.getValidUntil())
                .build();
    }

    public static CheckInAuditDTO toCheckInAuditDTO(CheckInAuditEntry entry) {
        return CheckInAuditDTO.builder()
                .id(entry.getId())
                .eventId(entry.getEvent() != null ? entry.getEvent().getId() : null)
                .gender(entry.getGender())
                .action(entry.getAction())
                .performedBy(entry.getPerformedBy())
                .timestamp(entry.getTimestamp())
                .build();
    }
}
