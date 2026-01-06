package tn.sesame.rh_management_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import tn.sesame.rh_management_backend.Enumerations.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String email,
        UserRole role,
        boolean mfaEnabled,
        boolean emailVerified,
        boolean accountLocked,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC") Instant lastLoginAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC") Instant createdAt
) {}
