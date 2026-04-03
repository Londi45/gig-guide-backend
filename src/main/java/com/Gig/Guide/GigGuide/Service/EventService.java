package com.Gig.Guide.GigGuide.Service;

import com.Gig.Guide.GigGuide.DTO.Event.*;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {

    // Core CRUD
    EventDTO createEvent(EventDTO dto, Long userId);
    EventDTO updateEvent(Long eventId, EventDTO dto, Long userId);
    void deleteEvent(Long eventId, Long userId);

    // Public browsing
    Page<EventDTO> getPublishedEvents(Pageable pageable);
    EventDTO getEventById(Long id);
    Page<EventDTO> getEventsByClub(Long clubId, Pageable pageable);
    Page<EventDTO> getEventsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Dashboard
    Page<EventDTO> getDashboardEvents(Long userId, EventStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Status transitions
    EventDTO transitionStatus(Long eventId, EventStatus newStatus, Long userId);

    // Entry types
    EntryTypeDTO createEntryType(Long eventId, EntryTypeDTO dto, Long userId);
    EntryTypeDTO updateEntryType(Long entryTypeId, EntryTypeDTO dto, Long userId);
    void deleteEntryType(Long entryTypeId, Long userId);

    // Discounts
    DiscountDTO createDiscount(Long eventId, DiscountDTO dto, Long userId);
    DiscountDTO updateDiscount(Long discountId, DiscountDTO dto, Long userId);
    void deleteDiscount(Long discountId, Long userId);

    // Live attendance
    Map<String, Object> checkIn(Long eventId, String gender, Long userId);
    Map<String, Object> checkOut(Long eventId, String gender, Long userId);
    Page<CheckInAuditDTO> getAuditLog(Long eventId, Long userId, Pageable pageable);

    // Staff assignments
    void assignStaff(Long eventId, Long staffUserId, Long requesterId);
    void removeStaffAssignment(Long eventId, Long staffUserId, Long requesterId);
    List<UserResponseDTO> getEventStaff(Long eventId);

    // Image handling
    EventDTO uploadImage(Long eventId, MultipartFile file, Long userId);
    EventDTO setImageUrl(Long eventId, String url, Long userId);
}
