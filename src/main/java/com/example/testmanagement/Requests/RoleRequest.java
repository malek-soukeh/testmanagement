package com.example.testmanagement.Requests;

import lombok.Data;

import java.util.Set;

@Data
public class RoleRequest {
    private String name;
    private Set<String> permissions;
}
