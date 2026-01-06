package tn.sesame.rh_management_backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String email;
    private String role;
    private boolean mfaEnabled;
    private boolean mfaRequired;
}
