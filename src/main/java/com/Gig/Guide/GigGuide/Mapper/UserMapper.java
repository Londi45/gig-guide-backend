package com.Gig.Guide.GigGuide.Mapper;

import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Models.Users.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .clubId(user.getClub() != null ? user.getClub().getId() : null)
                .active(user.isActive())
                .build();
    }
}
