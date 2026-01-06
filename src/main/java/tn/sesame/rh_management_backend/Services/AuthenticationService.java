package tn.sesame.rh_management_backend.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.sesame.rh_management_backend.Entities.User;
import tn.sesame.rh_management_backend.Repositories.UserRepository;
import tn.sesame.rh_management_backend.configurations.JwtUtil;
import tn.sesame.rh_management_backend.dto.*;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 15 * 60 * 1000; // 15 minutes

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new tn.sesame.rh_management_backend.exceptions.ConflictException("Email already in use");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .emailVerified(false)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .mfaEnabled(false)
                .createdAt(Instant.now())
                .build();

        user = userRepository.save(user);

        // Generate tokens
        String token = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .mfaEnabled(user.isMfaEnabled())
                .mfaRequired(false)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        
        if (user == null) {
            throw new tn.sesame.rh_management_backend.exceptions.UnauthorizedException("Invalid credentials");
        }

        // Check if account is locked
        if (user.isAccountLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(java.time.LocalDateTime.now())) {
                // Unlock account
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            } else {
                throw new tn.sesame.rh_management_backend.exceptions.ForbiddenException("Account is locked. Please try again later.");
            }
        }

        try {
            // Authenticate
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
            }

            // Update last login
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            // Check if MFA is enabled
            if (user.isMfaEnabled()) {
                if (request.getMfaCode() == null || request.getMfaCode().isEmpty()) {
                    return AuthResponse.builder()
                            .email(user.getEmail())
                            .role(user.getRole().name())
                            .mfaEnabled(true)
                            .mfaRequired(true)
                            .build();
                }
                // Here you would validate the MFA code
                // For now, we'll skip the actual validation
            }

            // Generate tokens
            String token = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            return AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .mfaEnabled(user.isMfaEnabled())
                    .mfaRequired(false)
                    .build();

        } catch (Exception e) {
            // Increment failed attempts
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockedUntil(java.time.LocalDateTime.now().plusMinutes(15));
            }

            userRepository.save(user);
            throw new tn.sesame.rh_management_backend.exceptions.UnauthorizedException("Invalid credentials");
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        try {
            String email = jwtUtil.extractUsername(request.getRefreshToken());
            User user = userRepository.findByEmail(email);

            if (user != null && jwtUtil.validateToken(request.getRefreshToken(), user)) {
                String newToken = jwtUtil.generateToken(user);
                String newRefreshToken = jwtUtil.generateRefreshToken(user);

                return AuthResponse.builder()
                        .token(newToken)
                        .refreshToken(newRefreshToken)
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .mfaEnabled(user.isMfaEnabled())
                        .mfaRequired(false)
                        .build();
            }
        } catch (Exception e) {
            throw new tn.sesame.rh_management_backend.exceptions.UnauthorizedException("Invalid refresh token");
        }

        throw new tn.sesame.rh_management_backend.exceptions.UnauthorizedException("Invalid refresh token");
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            throw new tn.sesame.rh_management_backend.exceptions.NotFoundException("User not found");
        }

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new tn.sesame.rh_management_backend.exceptions.BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateAt(Instant.now());
        userRepository.save(user);
    }
}
