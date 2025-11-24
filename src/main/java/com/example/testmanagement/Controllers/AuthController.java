package com.example.testmanagement.Controllers;

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
        if (authentication.getPrincipal() instanceof com.example.testmanagement.Security.CustomUserDetails) {
            com.example.testmanagement.Security.CustomUserDetails userDetails = 
                (com.example.testmanagement.Security.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("authorities", userDetails.getAuthorities().stream()
                    .map(auth -> {
                        Map<String, String> authMap = new HashMap<>();
                        authMap.put("authority", auth.getAuthority());
                        return authMap;
                    })
                    .collect(Collectors.toList()));
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(authentication.getPrincipal());
    }




}
