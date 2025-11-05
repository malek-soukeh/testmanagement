package com.example.testmanagement.Services;

import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.UserRepository;
import com.example.testmanagement.Requests.LoginRequest;
import com.example.testmanagement.Requests.SignUpRequest;
import com.example.testmanagement.Responses.JwtAuthenticationResponse;
import com.example.testmanagement.Security.CustomUserDetails;
import com.example.testmanagement.Security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse signin(LoginRequest request)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        CustomUserDetails userDetails = new CustomUserDetails(user);

        var jwt = jwtService.generateToken(userDetails);
        return JwtAuthenticationResponse.builder()
                .token(jwt)
                .build();
    }

    public JwtAuthenticationResponse signup(SignUpRequest request)
    { var user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
        userRepository.save(user);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        var jwt = jwtService.generateToken(userDetails);
        return JwtAuthenticationResponse.builder().token(jwt).build();

    }



}
