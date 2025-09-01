package com.example.testmanagement.Responses;

import com.example.testmanagement.Entities.Role;
import lombok.*;

import java.util.List;

@Setter
@Getter
public class LoginResponse {
    private String token;
    private Long id;
    private String email;
    private List<Role> roles;

    public LoginResponse(String token,Long id, String email, List<Role> roles) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }
}
