package com.example.testmanagement.Responses;

import lombok.Data;

import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled;
    private Set<String> roles;
}
