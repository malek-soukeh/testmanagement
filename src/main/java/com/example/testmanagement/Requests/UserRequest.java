package com.example.testmanagement.Requests;

import lombok.Data;

import java.util.Set;

@Data
public class UserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Set<String> roles;
}