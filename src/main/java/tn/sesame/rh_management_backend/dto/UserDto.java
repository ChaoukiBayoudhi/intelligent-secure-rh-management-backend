package tn.sesame.rh_management_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import tn.sesame.rh_management_backend.Enumerations.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * User Data Transfer Object
 * 
 * This DTO represents user data for API responses.
 * It includes user authentication and account status information.
 */
public record UserDto(
        UUID id,
        String email,
        UserRole role,
        boolean mfaEnabled,
        boolean emailVerified,
        boolean locked,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC") Instant lastLoginAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC") Instant createdAt
) {
}
