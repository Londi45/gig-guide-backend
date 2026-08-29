package com.Gig.Guide.GigGuide.Service;

import com.Gig.Guide.GigGuide.DTO.AuthResponseDTO;
import com.Gig.Guide.GigGuide.DTO.LoginRequestDTO;
import com.Gig.Guide.GigGuide.DTO.RegisterRequestDTO;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Enums.Role;
import com.Gig.Guide.GigGuide.Mapper.UserMapper;
import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import com.Gig.Guide.GigGuide.Models.Users.RefreshToken;
import com.Gig.Guide.GigGuide.Models.Users.User;
import com.Gig.Guide.GigGuide.Repositories.ClubRepository;
import com.Gig.Guide.GigGuide.Repositories.UserRepository;
import com.Gig.Guide.GigGuide.Service.IMPL.AuthServiceImpl;
import com.Gig.Guide.GigGuide.utils.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock private UserRepository userRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private JwtTokenUtil jwtTokenUtil;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private UserMapper userMapper;

    private RegisterRequestDTO clubOwnerDto;
    private RegisterRequestDTO staffDto;
    private User activeVerifiedUser;

    @BeforeEach
    void setUp() {
        clubOwnerDto = RegisterRequestDTO.builder()
                .username("owner1")
                .password("password123")
                .email("owner@test.com")
                .fullName("Owner One")
                .phoneNumber("+27821234567")
                .role("CLUB_OWNER")
                .build();

        staffDto = RegisterRequestDTO.builder()
                .username("staff1")
                .password("password123")
                .email("staff@test.com")
                .fullName("Staff One")
                .role("STAFF")
                .clubId("1")
                .build();

        activeVerifiedUser = User.builder()
                .id(1L)
                .email("owner@test.com")
                .password("hashedPassword")
                .role(Role.CLUB_OWNER)
                .isActive(true)
                .isVerified(true)
                .build();
    }

    // ─── register ────────────────────────────────────────────────────────────

    @Test
    void register_clubOwner_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPw");

        Clubs savedClub = new Clubs();
        savedClub.setId("1");
        when(clubRepository.save(any())).thenReturn(savedClub);

        User savedUser = User.builder().id(1L).email("owner@test.com").role(Role.CLUB_OWNER).build();
        when(userRepository.save(any())).thenReturn(savedUser);
        when(userMapper.toDTO(any())).thenReturn(new UserResponseDTO());
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        UserResponseDTO result = authService.register(clubOwnerDto);

        assertThat(result).isNotNull();
        verify(clubRepository, atLeastOnce()).save(any());
        verify(userRepository, atLeastOnce()).save(any());
        verify(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void register_staff_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPw");

        Clubs club = new Clubs();
        club.setId("1");
        when(clubRepository.findById("1")).thenReturn(Optional.of(club));

        User savedUser = User.builder().id(2L).email("staff@test.com").role(Role.STAFF).build();
        when(userRepository.save(any())).thenReturn(savedUser);
        when(userMapper.toDTO(any())).thenReturn(new UserResponseDTO());
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        UserResponseDTO result = authService.register(staffDto);

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("owner@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(clubOwnerDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void register_invalidRole_throwsBadRequest() {
        clubOwnerDto.setRole("SUPERUSER");

        assertThatThrownBy(() -> authService.register(clubOwnerDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    void register_adminRole_throwsBadRequest() {
        clubOwnerDto.setRole("ADMIN");

        assertThatThrownBy(() -> authService.register(clubOwnerDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    void register_staffWithoutClubId_throwsBadRequest() {
        staffDto.setClubId(null);

        assertThatThrownBy(() -> authService.register(staffDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("clubId is required");
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    void login_success() {
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(activeVerifiedUser));
        when(passwordEncoder.matches("mypassword", "hashedPassword")).thenReturn(true);
        when(jwtTokenUtil.generateToken(anyString(), anyString())).thenReturn("access-token");

        RefreshToken rt = RefreshToken.builder().token("refresh-token").build();
        when(refreshTokenService.createRefreshToken(any())).thenReturn(rt);

        AuthResponseDTO result = authService.login(new LoginRequestDTO("owner@test.com", "mypassword"));

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getRole()).isEqualTo("CLUB_OWNER");
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(activeVerifiedUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("owner@test.com", "wrong")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_unverifiedAccount_throwsForbidden() {
        User unverified = User.builder()
                .email("owner@test.com").password("hash")
                .isVerified(false).isActive(true).role(Role.CLUB_OWNER).build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(unverified));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("owner@test.com", "pass")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not verified");
    }

    @Test
    void login_deactivatedAccount_throwsForbidden() {
        User deactivated = User.builder()
                .email("owner@test.com").password("hash")
                .isVerified(true).isActive(false).role(Role.CLUB_OWNER).build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(deactivated));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("owner@test.com", "pass")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("deactivated");
    }

    // ─── verifyEmail ──────────────────────────────────────────────────────────

    @Test
    void verifyEmail_success() {
        User user = User.builder()
                .id(1L).verificationToken("valid-token")
                .verificationTokenExpiry(LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        authService.verifyEmail("valid-token");

        assertThat(user.isVerified()).isTrue();
        assertThat(user.getVerificationToken()).isNull();
    }

    @Test
    void verifyEmail_expiredToken_throwsBadRequest() {
        User user = User.builder()
                .id(1L).verificationToken("expired-token")
                .verificationTokenExpiry(LocalDateTime.now().minusHours(1))
                .build();

        when(userRepository.findByVerificationToken("expired-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyEmail("expired-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("expired");
    }

    // ─── resetPassword ────────────────────────────────────────────────────────

    @Test
    void resetPassword_success() {
        User user = User.builder()
                .id(1L).passwordResetToken("reset-token")
                .passwordResetTokenExpiry(LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByPasswordResetToken("reset-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass123")).thenReturn("newHash");
        when(userRepository.save(any())).thenReturn(user);

        authService.resetPassword("reset-token", "newPass123");

        assertThat(user.getPasswordResetToken()).isNull();
        assertThat(user.getPassword()).isEqualTo("newHash");
    }

    @Test
    void resetPassword_tooShort_throwsBadRequest() {
        User user = User.builder()
                .id(1L).passwordResetToken("reset-token")
                .passwordResetTokenExpiry(LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByPasswordResetToken("reset-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword("reset-token", "short"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("8 characters");
    }
}
