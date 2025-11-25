package tn.sesame.rh_management_backend.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import tn.sesame.rh_management_backend.Enumerations.UserRole;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Accessors(fluent = true)//calling the getters and setters without using the prefixes get and set
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(staticName = "of")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude={"password","mfaSecret"})
@EqualsAndHashCode(of="email")
@Table(name = "users",
uniqueConstraints = @UniqueConstraint(columnNames = {"email"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @NonNull
    @Email
    String email;
    @NonNull
    @Size(min = 8)
    @JsonIgnore
    String password;
    @JsonIgnore
    Set<UserRole> roles=new HashSet<>();
    boolean mfaEnabled;
    String mfaSecret;
    @JsonIgnore
    boolean emailVerified;
    boolean accountLocked;
    int failedLoginAttempts;
    Instant lastLoginAt;
    Instant createdAt;
    Instant updateAt;
    LocalDateTime lockedUntil;

    //the relationship with User
    @OneToOne(mappedBy = "user")
    Employee employee;
}
