package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.Role;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Requests.LoginRequest;
import com.example.testmanagement.Requests.SignUpRequest;
import com.example.testmanagement.Responses.JwtAuthenticationResponse;
import com.example.testmanagement.Services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService  authService;


    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.signin(request));
    }
    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponse> signup(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        return ResponseEntity.ok(authentication.getPrincipal());
    }




}
