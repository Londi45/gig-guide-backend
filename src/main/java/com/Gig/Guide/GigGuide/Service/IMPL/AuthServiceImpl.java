package com.Gig.Guide.GigGuide.Service.IMPL;

import com.Gig.Guide.GigGuide.DTO.AuthResponseDTO;
import com.Gig.Guide.GigGuide.DTO.LoginRequestDTO;
import com.Gig.Guide.GigGuide.DTO.RegisterRequestDTO;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Enums.Role;
import com.Gig.Guide.GigGuide.Exceptions.ResourceNotFoundException;
import com.Gig.Guide.GigGuide.Mapper.UserMapper;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import com.Gig.Guide.GigGuide.Models.Users.RefreshToken;
import com.Gig.Guide.GigGuide.Models.Users.User;
import com.Gig.Guide.GigGuide.Repositories.ClubRepository;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.AuthService;
import com.Gig.Guide.GigGuide.Service.EmailService;
import com.Gig.Guide.GigGuide.Service.RefreshTokenService;
import com.Gig.Guide.GigGuide.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserResponseDTO register(RegisterRequestDTO dto) {
        // Validate role
        Role role;
        try {
            role = Role.valueOf(dto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role specified");
        }

        if (role == Role.ADMIN || (role != Role.CLUB_OWNER && role != Role.STAFF)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role specified");
        }

        // Check email uniqueness
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

        User user;

        if (role == Role.CLUB_OWNER) {
            // Create club
            Clubs club = new Clubs();
            club.setName(dto.getFullName());
            club.setEmail(dto.getEmail());
            club.setPhone(dto.getPhoneNumber());
            club.setActive(true);
            Clubs savedClub = clubRepository.save(club);

            user = User.builder()
                    .username(dto.getUsername())
                    .email(dto.getEmail())
                    .password(hashedPassword)
                    .fullName(dto.getFullName())
                    .phoneNumber(dto.getPhoneNumber())
                    .role(role)
                    .club(savedClub)
                    .isActive(true)
                    .verificationToken(verificationToken)
                    .verificationTokenExpiry(tokenExpiry)
                    .isVerified(false)
                    .build();

            // Link owner to club
            User savedUser = userRepository.save(user);
            savedClub.setOwner(savedUser);
            clubRepository.save(savedClub);
            user = savedUser;

        } else {
            // STAFF — find club by clubId
            if (dto.getClubId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clubId is required for STAFF role");
            }
            Clubs club = clubRepository.findById(dto.getClubId())
                    .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

            user = User.builder()
                    .username(dto.getUsername())
                    .email(dto.getEmail())
                    .password(hashedPassword)
                    .fullName(dto.getFullName())
                    .phoneNumber(dto.getPhoneNumber())
                    .role(role)
                    .club(club)
                    .isActive(true)
                    .verificationToken(verificationToken)
                    .verificationTokenExpiry(tokenExpiry)
                    .isVerified(false)
                    .build();

            user = userRepository.save(user);
        }

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        return userMapper.toDTO(user);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!user.isVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not verified");
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }

        String accessToken = jwtTokenUtil.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponseDTO refresh(String refreshToken) {
        User user = refreshTokenService.validateRefreshToken(refreshToken);
        String newAccessToken = jwtTokenUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (user.getVerificationTokenExpiry() != null
                && user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token has expired");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendForgotPasswordEmail(email, resetToken);
        });
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset token"));

        if (user.getPasswordResetTokenExpiry() != null
                && user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token has expired");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }
}
