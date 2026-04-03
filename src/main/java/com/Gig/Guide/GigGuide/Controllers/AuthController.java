package com.Gig.Guide.GigGuide.Controllers;

import com.Gig.Guide.GigGuide.DTO.AuthResponseDTO;
import com.Gig.Guide.GigGuide.DTO.ForgotPasswordRequestDTO;
import com.Gig.Guide.GigGuide.DTO.LoginRequestDTO;
import com.Gig.Guide.GigGuide.DTO.PasswordResetRequestDTO;
import com.Gig.Guide.GigGuide.DTO.RegisterRequestDTO;
import com.Gig.Guide.GigGuide.DTO.UserResponseDTO;
import com.Gig.Guide.GigGuide.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        authService.forgotPassword(dto.getEmail());
        return ResponseEntity.ok("If that email is registered, a reset link has been sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequestDTO dto) {
        authService.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
    }
}
