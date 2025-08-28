package com.example.testmanagement.Responses;

import lombok.*;

import java.util.List;

@Setter
@Getter
public class LoginResponse {
    private String token;
    private Long id;
    private String email;
    private List<String> roles;

    public LoginResponse(String token,Long id, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }
}
