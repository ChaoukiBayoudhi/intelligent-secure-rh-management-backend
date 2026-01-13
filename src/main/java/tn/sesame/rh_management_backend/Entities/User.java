package tn.sesame.rh_management_backend.Entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.sesame.rh_management_backend.Enumerations.UserRole;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Builder
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(staticName = "of")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude={"password","mfaSecret"})
@EqualsAndHashCode(of="email")
@Table(name = "users",
uniqueConstraints = @UniqueConstraint(columnNames = {"email"}))
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NonNull
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    String email;

    // Password field - nullable for OAuth2 users who don't have passwords
    // Validation is handled at the service/DTO level, not entity level
    // This allows OAuth2 users to have null/empty passwords
    @JsonIgnore
    String password;
    
    // Track OAuth2 provider for users who login via OAuth2
    // This helps identify authentication method and enables account linking
    @Column(name = "oauth2_provider")
    String oauth2Provider;

//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
//    @Enumerated(EnumType.STRING)
//    @JsonIgnore
//    Set<UserRole> roles=new HashSet<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)               // 1. Enforces NOT NULL in the Database Schema
    @NotNull(message = "Role is required")  // 2. Returns 400 Bad Request if missing in JSON
    @NonNull
    UserRole role;

    boolean mfaEnabled;
    String mfaSecret;
    @JsonIgnore
    boolean emailVerified;
    boolean accountLocked;
    int failedLoginAttempts;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    Instant lastLoginAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    Instant createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    Instant updateAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    LocalDateTime lockedUntil;

    //the relationship with User
    @OneToOne(mappedBy = "user",fetch = FetchType.LAZY)
    @JsonIgnoreProperties("user")
    Employee employee;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
