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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
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
        log.info("Registering user - email={}, role={}", dto.getEmail(), dto.getRole());
        // Validate role
        Role role;
        try {
            role = Role.valueOf(dto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role specified - role={}", dto.getRole());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role specified");
        }

        if (role == Role.ADMIN || (role != Role.CLUB_OWNER && role != Role.STAFF)) {
            log.warn("Rejected registration - role not allowed: {}", role);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role specified");
        }

        // Check email uniqueness
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registration failed - email already exists: {}", dto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

        User user;

        if (role == Role.CLUB_OWNER) {
            log.info("Creating club for new CLUB_OWNER - email={}", dto.getEmail());
            // Create club
            Clubs club = new Clubs();
            club.setName(dto.getFullName());
            club.setEmail(dto.getEmail());
            club.setPhone(dto.getPhoneNumber());
            club.setActive(true);
            Clubs savedClub = clubRepository.save(club);
            log.info("Club created - clubId={}", savedClub.getId());

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
            log.info("CLUB_OWNER registered - userId={}, clubId={}", user.getId(), savedClub.getId());

        } else {
            // STAFF — find club by clubId
            if (dto.getClubId() == null) {
                log.warn("STAFF registration missing clubId - email={}", dto.getEmail());
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
            log.info("STAFF registered - userId={}, clubId={}", user.getId(), club.getId());
        }

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        log.info("Verification email sent - userId={}, email={}", user.getId(), user.getEmail());
        return userMapper.toDTO(user);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        log.info("Login attempt - email={}", dto.getEmail());
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for email={}", dto.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!user.isVerified()) {
            log.warn("Login denied - account not verified email={}", dto.getEmail());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not verified");
        }

        if (!user.isActive()) {
            log.warn("Login denied - account deactivated email={}", dto.getEmail());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }

        String accessToken = jwtTokenUtil.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        log.info("Login successful - userId={}, role={}", user.getId(), user.getRole());

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponseDTO refresh(String refreshToken) {
        log.info("Token refresh requested");
        User user = refreshTokenService.validateRefreshToken(refreshToken);
        String newAccessToken = jwtTokenUtil.generateToken(user.getEmail(), user.getRole().name());
        log.info("Token refreshed - userId={}", user.getId());

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        log.info("Logout requested - revoking refresh token");
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("Refresh token revoked");
    }

    @Override
    public void verifyEmail(String token) {
        log.info("Email verification attempt");
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (user.getVerificationTokenExpiry() != null
                && user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Email verification failed - token expired for userId={}", user.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token has expired");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        log.info("Email verified - userId={}", user.getId());
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Forgot password requested - email={}", email);
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendForgotPasswordEmail(email, resetToken);
            log.info("Password reset email sent - userId={}", user.getId());
        });
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Password reset attempt");
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset token"));

        if (user.getPasswordResetTokenExpiry() != null
                && user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Password reset failed - token expired for userId={}", user.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token has expired");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        log.info("Password reset successful - userId={}", user.getId());
    }
}
