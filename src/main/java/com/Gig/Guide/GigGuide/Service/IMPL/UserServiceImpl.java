package com.Gig.Guide.GigGuide.Service.IMPL;

import com.Gig.Guide.GigGuide.DTO.RegisterRequestDTO;
import com.Gig.Guide.GigGuide.DTO.UpdateProfileDTO;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Enums.Role;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Mapper.UserMapper;
import com.Gig.Guide.GigGuide.Models.Users.User;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserResponseDTO createStaff(RegisterRequestDTO dto, Long requesterId) {
        log.info("Creating staff - requesterId={}, email={}", requesterId, dto.getEmail());
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        if (requester.getRole() != Role.CLUB_OWNER) {
            log.warn("Unauthorized staff creation attempt - requesterId={}, role={}", requesterId, requester.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff are not permitted to manage user accounts");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Staff creation failed - email already exists: {}", dto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User staff = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .role(Role.STAFF)
                .club(requester.getClub())
                .isActive(true)
                .isVerified(true)
                .build();

        User saved = userRepository.save(staff);
        log.info("Staff created - staffId={}, clubId={}", saved.getId(), requester.getClub() != null ? requester.getClub().getId() : null);
        return userMapper.toDTO(saved);
    }

    @Override
    public void deactivateStaff(Long userId, Long requesterId) {
        log.info("Deactivating staff - staffId={}, requesterId={}", userId, requesterId);
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        if (requester.getRole() != Role.CLUB_OWNER) {
            log.warn("Unauthorized deactivation attempt - requesterId={}, role={}", requesterId, requester.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff are not permitted to manage user accounts");
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (requester.getClub() == null || target.getClub() == null
                || !requester.getClub().getId().equals(target.getClub().getId())) {
            log.warn("Cross-club deactivation denied - requesterId={}, targetId={}", requesterId, userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot deactivate staff from another club");
        }

        target.setActive(false);
        userRepository.save(target);
        log.info("Staff deactivated - staffId={}", userId);
    }

    @Override
    public UserResponseDTO updateProfile(Long userId, UpdateProfileDTO dto) {
        log.info("Updating profile - userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        User saved = userRepository.save(user);
        log.info("Profile updated - userId={}", userId);
        return userMapper.toDTO(saved);
    }

    @Override
    public List<UserResponseDTO> getStaffByClub(Long requesterId) {
        log.info("Fetching staff for requesterId={}", requesterId);
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (requester.getClub() == null) {
            log.warn("User not linked to a club - requesterId={}", requesterId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not linked to a club");
        }

        List<UserResponseDTO> staff = userRepository.findByClubIdAndRole(requester.getClub().getId(), Role.STAFF)
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
        log.info("Fetched {} staff for clubId={}", staff.size(), requester.getClub().getId());
        return staff;
    }
}
