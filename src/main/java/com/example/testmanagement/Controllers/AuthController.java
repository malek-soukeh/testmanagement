package com.example.testmanagement.Controllers;

import com.example.testmanagement.Entities.Role;
import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.UserRepository;
import com.example.testmanagement.Requests.LoginRequest;
import com.example.testmanagement.Responses.LoginResponse;
import com.example.testmanagement.Security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil,
                          UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return "User registered!";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepo.findByEmail(request.getEmail()).orElseThrow(()-> new RuntimeException("User not found!"));
        String token = jwtUtil.generateToken(request.getEmail());
        List<Role> roles = user.getRoles().stream().toList();
        return new LoginResponse(token, user.getId(), user.getEmail(),  roles);
    }
}
