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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

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
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        if (requester.getRole() != Role.CLUB_OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff are not permitted to manage user accounts");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
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

        return userMapper.toDTO(userRepository.save(staff));
    }

    @Override
    public void deactivateStaff(Long userId, Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        if (requester.getRole() != Role.CLUB_OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff are not permitted to manage user accounts");
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (requester.getClub() == null || target.getClub() == null
                || !requester.getClub().getId().equals(target.getClub().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot deactivate staff from another club");
        }

        target.setActive(false);
        userRepository.save(target);
    }

    @Override
    public UserResponseDTO updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public List<UserResponseDTO> getStaffByClub(Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (requester.getClub() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not linked to a club");
        }

        return userRepository.findByClubIdAndRole(requester.getClub().getId(), Role.STAFF)
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }
}
