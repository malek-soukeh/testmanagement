package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.Role;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.UserRepository;
import com.example.testmanagement.Requests.UserRequest;
import com.example.testmanagement.Responses.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByEmail(username);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        if (request.getRole() != null) {
            try {
                user.setRole(Role.valueOf(request.getRole()));
            } catch (IllegalArgumentException e) {
                user.setRole(Role.ROLE_TESTER); // Default role if invalid
            }
        } else {
            user.setRole(Role.ROLE_TESTER);
        }

        User savedUser = userRepository.save(user);
        auditLogService.logAction("CREATE", "USER", savedUser.getId().toString(),
                "Created user: " + savedUser.getEmail());
        return mapToResponse(savedUser);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            try {
                user.setRole(Role.valueOf(request.getRole()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid role or keep existing
            }
        }

        User updatedUser = userRepository.save(user);
        auditLogService.logAction("UPDATE", "USER", updatedUser.getId().toString(),
                "Updated user: " + updatedUser.getEmail());
        return mapToResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        auditLogService.logAction("DELETE", "USER", id.toString(), "Deleted user with ID: " + id);
        userRepository.deleteById(id);
    }

    public void toggleUserAccess(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        res.setEmail(user.getEmail());
        res.setEnabled(user.isEnabled());
        res.setRole(user.getRole().name());
        return res;
    }

}
