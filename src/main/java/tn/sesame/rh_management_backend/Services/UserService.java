package tn.sesame.rh_management_backend.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.sesame.rh_management_backend.Entities.User;
import tn.sesame.rh_management_backend.Repositories.UserRepository;
import tn.sesame.rh_management_backend.dto.UserDTO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new tn.sesame.rh_management_backend.exceptions.NotFoundException("User not found");
        }
        return convertToDTO(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("User not found"));
        userRepository.delete(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDTO lockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("User not found"));
        user.setAccountLocked(true);
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDTO unlockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("User not found"));
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isMfaEnabled(),
                user.isEmailVerified(),
                user.isAccountLocked(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
