package com.Gig.Guide.GigGuide.Service;

import com.Gig.Guide.GigGuide.DTO.RegisterRequestDTO;
import com.Gig.Guide.GigGuide.DTO.UpdateProfileDTO;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createStaff(RegisterRequestDTO dto, Long requesterId);

    void deactivateStaff(Long userId, Long requesterId);

    UserResponseDTO updateProfile(Long userId, UpdateProfileDTO dto);

    List<UserResponseDTO> getStaffByClub(Long requesterId);
}
