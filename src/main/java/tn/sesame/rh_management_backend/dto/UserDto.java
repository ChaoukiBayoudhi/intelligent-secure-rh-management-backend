package tn.sesame.rh_management_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import tn.sesame.rh_management_backend.Enumerations.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(UUID id,
                      String email,
                      UserRole role,
                      boolean locked,
                      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lockedUntil
                      ) {
}
