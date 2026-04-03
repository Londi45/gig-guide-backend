package com.Gig.Guide.GigGuide.Service;

import com.Gig.Guide.GigGuide.DTO.AuthResponseDTO;
import com.Gig.Guide.GigGuide.DTO.LoginRequestDTO;
import com.Gig.Guide.GigGuide.DTO.RegisterRequestDTO;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;

public interface AuthService {

    UserResponseDTO register(RegisterRequestDTO dto);

    AuthResponseDTO login(LoginRequestDTO dto);

    AuthResponseDTO refresh(String refreshToken);

    void logout(String refreshToken);

    void verifyEmail(String token);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}
