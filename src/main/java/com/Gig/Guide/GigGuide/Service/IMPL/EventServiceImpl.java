package com.Gig.Guide.GigGuide.Service.IMPL;

import com.Gig.Guide.GigGuide.DTO.Event.*;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Enums.EventStatus;
import com.Gig.Guide.GigGuide.Enums.Role;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Mapper.EventMapper;
import com.Gig.Guide.GigGuide.Mapper.UserMapper;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import com.Gig.Guide.GigGuide.Models.Event.*;
import com.Gig.Guide.GigGuide.Models.Users.User;
import com.Gig.Guide.GigGuide.Repositories.*;
import com.Gig.Guide.GigGuide.Service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private EntryTypeRepository entryTypeRepository;

    @Autowired
    private StaffAssignmentRepository staffAssignmentRepository;

    @Autowired
    private CheckInAuditRepository checkInAuditRepository;

    @Autowired
    private UserMapper userMapper;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ─── Helper ──────────────────────────────────────────────────────────────

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private void assertClubOwnership(Event event, User user) {
        if (user.getRole() == Role.ADMIN) return;
        if (user.getClub() == null || event.getClub() == null
                || !user.getClub().getId().equals(event.getClub().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: event does not belong to your club");
        }
    }

    // ─── Core CRUD ───────────────────────────────────────────────────────────

    @Override
    public EventDTO createEvent(EventDTO dto, Long userId) {
        User user = getUser(userId);

        Clubs club;
        if (user.getRole() == Role.ADMIN) {
            club = clubRepository.findById(dto.getClubId())
                    .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
        } else {
            if (user.getClub() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not linked to a club");
            }
            club = user.getClub();
        }

        if (dto.getEndDateTime() == null || dto.getStartDateTime() == null
                || !dto.getEndDateTime().isAfter(dto.getStartDateTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        if (dto.getCapacity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capacity must be at least 1");
        }

        int maleRatio = dto.getMaleRatio();
        int femaleRatio = dto.getFemaleRatio();

        if (maleRatio == 0 && femaleRatio == 0) {
            maleRatio = 50;
            femaleRatio = 50;
        } else if (maleRatio + femaleRatio != 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Male and female ratios must sum to 100");
        }

        Event event = Event.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .genre(dto.getGenre())
                .dressCode(dto.getDressCode())
                .ageRestriction(dto.getAgeRestriction())
                .imageUrl(dto.getImageUrl())
                .status(EventStatus.DRAFT)
                .capacity(dto.getCapacity())
                .maleRatio(maleRatio)
                .femaleRatio(femaleRatio)
                .club(club)
                .build();

        return EventMapper.toDTO(eventRepository.save(event));
    }

    @Override
    public EventDTO updateEvent(Long eventId, EventDTO dto, Long userId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        assertClubOwnership(event, user);

        if (dto.getName() != null) event.setName(dto.getName());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getGenre() != null) event.setGenre(dto.getGenre());
        if (dto.getDressCode() != null) event.setDressCode(dto.getDressCode());
        if (dto.getAgeRestriction() != null) event.setAgeRestriction(dto.getAgeRestriction());
        if (dto.getImageUrl() != null) event.setImageUrl(dto.getImageUrl());

        if (dto.getStartDateTime() != null) event.setStartDateTime(dto.getStartDateTime());
        if (dto.getEndDateTime() != null) event.setEndDateTime(dto.getEndDateTime());

        if (event.getEndDateTime() != null && event.getStartDateTime() != null
                && !event.getEndDateTime().isAfter(event.getStartDateTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        if (dto.getCapacity() > 0) {
            if (dto.getCapacity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capacity must be at least 1");
            }
            event.setCapacity(dto.getCapacity());
        }

        if (dto.getMaleRatio() != 0 || dto.getFemaleRatio() != 0) {
            if (dto.getMaleRatio() + dto.getFemaleRatio() != 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Male and female ratios must sum to 100");
            }
            event.setMaleRatio(dto.getMaleRatio());
            event.setFemaleRatio(dto.getFemaleRatio());
        }

        return EventMapper.toDTO(eventRepository.save(event));
    }

    @Override
    public void deleteEvent(Long eventId, Long userId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);

        if (user.getRole() == Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff are not permitted to delete events");
        }

        assertClubOwnership(event, user);
        eventRepository.delete(event);
    }

    // ─── Public Browsing ─────────────────────────────────────────────────────

    @Override
    public Page<EventDTO> getPublishedEvents(Pageable pageable) {
        return eventRepository.findByStatusAndStartDateTimeAfter(EventStatus.PUBLISHED, LocalDateTime.now(), pageable)
                .map(e -> EventMapper.toDTO(e, true));
    }

    @Override
    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Event not found");
        }

        return EventMapper.toDTO(event, true);
    }

    @Override
    public Page<EventDTO> getEventsByClub(Long clubId, Pageable pageable) {
        return eventRepository.findByClubIdAndStatus(clubId, EventStatus.PUBLISHED, pageable)
                .map(e -> EventMapper.toDTO(e, true));
    }

    @Override
    public Page<EventDTO> getEventsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return eventRepository.findByStatusAndStartDateTimeBetween(EventStatus.PUBLISHED, start, end, pageable)
                .map(e -> EventMapper.toDTO(e, true));
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    @Override
    public Page<EventDTO> getDashboardEvents(Long userId, EventStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        User user = getUser(userId);
        if (user.getClub() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not linked to a club");
        }
        Long clubId = user.getClub().getId();

        if (status != null && startDate != null && endDate != null) {
            return eventRepository.findByClubIdAndStatusAndStartDateTimeBetween(clubId, status, startDate, endDate, pageable)
                    .map(EventMapper::toDTO);
        } else if (status != null) {
            return eventRepository.findByClubIdAndStatus(clubId, status, pageable)
                    .map(EventMapper::toDTO);
        } else if (startDate != null && endDate != null) {
            return eventRepository.findByClubIdAndStartDateTimeBetween(clubId, startDate, endDate, pageable)
                    .map(EventMapper::toDTO);
        } else {
            return eventRepository.findByClubId(clubId, pageable)
                    .map(EventMapper::toDTO);
        }
    }

    // ─── Status Transitions ──────────────────────────────────────────────────

    @Override
    public EventDTO transitionStatus(Long eventId, EventStatus newStatus, Long userId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        assertClubOwnership(event, user);

        if (user.getRole() == Role.STAFF
                && (newStatus == EventStatus.CANCELLED || newStatus == EventStatus.COMPLETED)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff are not permitted to cancel or complete events");
        }

        event.setStatus(newStatus);
        return EventMapper.toDTO(eventRepository.save(event));
    }

    // ─── Entry Types ─────────────────────────────────────────────────────────

    @Override
    public EntryTypeDTO createEntryType(Long eventId, EntryTypeDTO dto, Long userId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        assertClubOwnership(event, user);

        if (dto.getPrice() != null && dto.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry type price must be non-negative");
        }
        if (dto.getAvailableQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Available quantity must be non-negative");
        }

        EntryType entryType = EntryType.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .availableQuantity(dto.getAvailableQuantity())
                .event(event)
                .build();

        return EventMapper.toEntryTypeDTO(entryTypeRepository.save(entryType));
    }

    @Override
    public EntryTypeDTO updateEntryType(Long entryTypeId, EntryTypeDTO dto, Long userId) {
        EntryType entryType = entryTypeRepository.findById(entryTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry type not found"));
        User user = getUser(userId);
        assertClubOwnership(entryType.getEvent(), user);

        if (dto.getName() != null) entryType.setName(dto.getName());
        if (dto.getPrice() != null) {
            if (dto.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry type price must be non-negative");
            }
            entryType.setPrice(dto.getPrice());
        }
        if (dto.getDescription() != null) entryType.setDescription(dto.getDescription());
        if (dto.getAvailableQuantity() >= 0) entryType.setAvailableQuantity(dto.getAvailableQuantity());

        return EventMapper.toEntryTypeDTO(entryTypeRepository.save(entryType));
    }

    @Override
    public void deleteEntryType(Long entryTypeId, Long userId) {
        EntryType entryType = entryTypeRepository.findById(entryTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry type not found"));
        User user = getUser(userId);
        assertClubOwnership(entryType.getEvent(), user);
        entryTypeRepository.delete(entryType);
    }

    // ─── Discounts ───────────────────────────────────────────────────────────

    @Override
    public DiscountDTO createDiscount(Long eventId, DiscountDTO dto, Long userId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        assertClubOwnership(event, user);

        if (dto.getValidFrom() != null && dto.getValidUntil() != null
                && !dto.getValidUntil().isAfter(dto.getValidFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount end time must be after start time");
        }
        if (dto.getDiscountValue() != null && dto.getDiscountValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount value must be non-negative");
        }

        Discount discount = Discount.builder()
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .description(dto.getDescription())
                .validFrom(dto.getValidFrom())
                .validUntil(dto.getValidUntil())
                .event(event)
                .build();

        return EventMapper.toDiscountDTO(discountRepository.save(discount));
    }

    @Override
    public DiscountDTO updateDiscount(Long discountId, DiscountDTO dto, Long userId) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
        User user = getUser(userId);
        assertClubOwnership(discount.getEvent(), user);

        if (dto.getDiscountType() != null) discount.setDiscountType(dto.getDiscountType());
        if (dto.getDiscountValue() != null) {
            if (dto.getDiscountValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount value must be non-negative");
            }
            discount.setDiscountValue(dto.getDiscountValue());
        }
        if (dto.getDescription() != null) discount.setDescription(dto.getDescription());
        if (dto.getValidFrom() != null) discount.setValidFrom(dto.getValidFrom());
        if (dto.getValidUntil() != null) discount.setValidUntil(dto.getValidUntil());

        if (discount.getValidFrom() != null && discount.getValidUntil() != null
                && !discount.getValidUntil().isAfter(discount.getValidFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount end time must be after start time");
        }

        return EventMapper.toDiscountDTO(discountRepository.save(discount));
    }

    @Override
    public void deleteDiscount(Long discountId, Long userId) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
        User user = getUser(userId);
        assertClubOwnership(discount.getEvent(), user);
        discountRepository.delete(discount);
    }

    // ─── Live Attendance ─────────────────────────────────────────────────────

    @Override
    public Map<String, Object> checkIn(Long eventId, String gender, Long userId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        assertClubOwnership(event, user);

        if ("MALE".equalsIgnoreCase(gender)) {
            event.setLiveMaleCount(event.getLiveMaleCount() + 1);
        } else {
            event.setLiveFemaleCount(event.getLiveFemaleCount() + 1);
        }

        CheckInAuditEntry audit = CheckInAuditEntry.builder()
                .event(event)
                .gender(gender.toUpperCase())
                .action("CHECK_IN")
                .performedBy(userId)
                .timestamp(LocalDateTime.now())
                .build();
        checkInAuditRepository.save(audit);

        Event saved = eventRepository.save(event);
        return buildAttendanceMap(saved);
    }

    @Override
    public Map<String, Object> checkOut(Long eventId, String gender, Long userId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        assertClubOwnership(event, user);

        if ("MALE".equalsIgnoreCase(gender)) {
            if (event.getLiveMaleCount() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Count cannot go below zero");
            }
            event.setLiveMaleCount(event.getLiveMaleCount() - 1);
        } else {
            if (event.getLiveFemaleCount() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Count cannot go below zero");
            }
            event.setLiveFemaleCount(event.getLiveFemaleCount() - 1);
        }

        CheckInAuditEntry audit = CheckInAuditEntry.builder()
                .event(event)
                .gender(gender.toUpperCase())
                .action("CHECK_OUT")
                .performedBy(userId)
                .timestamp(LocalDateTime.now())
                .build();
        checkInAuditRepository.save(audit);

        Event saved = eventRepository.save(event);
        return buildAttendanceMap(saved);
    }

    private Map<String, Object> buildAttendanceMap(Event event) {
        Map<String, Object> result = new HashMap<>();
        result.put("liveMaleCount", event.getLiveMaleCount());
        result.put("liveFemaleCount", event.getLiveFemaleCount());
        result.put("liveTotalCount", event.getLiveMaleCount() + event.getLiveFemaleCount());
        return result;
    }

    @Override
    public Page<CheckInAuditDTO> getAuditLog(Long eventId, Long userId, Pageable pageable) {
        User user = getUser(userId);

        if (user.getRole() == Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff are not permitted to access audit logs");
        }

        Event event = getEvent(eventId);
        assertClubOwnership(event, user);

        return checkInAuditRepository.findByEventIdOrderByTimestampDesc(eventId, pageable)
                .map(EventMapper::toCheckInAuditDTO);
    }

    // ─── Staff Assignments ───────────────────────────────────────────────────

    @Override
    public void assignStaff(Long eventId, Long staffUserId, Long requesterId) {
        User requester = getUser(requesterId);
        if (requester.getRole() != Role.CLUB_OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only club owners can assign staff");
        }

        User staffUser = getUser(staffUserId);
        if (staffUser.getRole() != Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target user is not a staff member");
        }

        if (requester.getClub() == null || staffUser.getClub() == null
                || !requester.getClub().getId().equals(staffUser.getClub().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff member does not belong to your club");
        }

        Event event = getEvent(eventId);
        if (requester.getClub() == null || event.getClub() == null
                || !requester.getClub().getId().equals(event.getClub().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Event does not belong to your club");
        }

        if (!staffAssignmentRepository.existsByEventIdAndUserId(eventId, staffUserId)) {
            StaffAssignment assignment = StaffAssignment.builder()
                    .event(event)
                    .user(staffUser)
                    .build();
            staffAssignmentRepository.save(assignment);
        }
    }

    @Override
    public void removeStaffAssignment(Long eventId, Long staffUserId, Long requesterId) {
        User requester = getUser(requesterId);
        if (requester.getRole() != Role.CLUB_OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only club owners can remove staff assignments");
        }

        Event event = getEvent(eventId);
        assertClubOwnership(event, requester);
        staffAssignmentRepository.deleteByEventIdAndUserId(eventId, staffUserId);
    }

    @Override
    public List<UserResponseDTO> getEventStaff(Long eventId) {
        return staffAssignmentRepository.findByEventId(eventId).stream()
                .map(sa -> userMapper.toDTO(sa.getUser()))
                .collect(Collectors.toList());
    }

    // ─── Image Handling ──────────────────────────────────────────────────────

    @Override
    public EventDTO uploadImage(Long eventId, MultipartFile file, Long userId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        assertClubOwnership(event, user);

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file size exceeds the maximum allowed limit");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, file.getBytes());

            event.setImageUrl(baseUrl + "/uploads/" + filename);
            return EventMapper.toDTO(eventRepository.save(event));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }
    }

    @Override
    public EventDTO setImageUrl(Long eventId, String url, Long userId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        assertClubOwnership(event, user);
        event.setImageUrl(url);
        return EventMapper.toDTO(eventRepository.save(event));
    }
}
