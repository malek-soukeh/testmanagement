package com.example.testmanagement.Responses;

import lombok.Data;

import java.util.Set;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    private Set<String> permissions;
}
