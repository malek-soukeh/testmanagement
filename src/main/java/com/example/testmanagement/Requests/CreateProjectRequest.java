package com.example.testmanagement.Requests;

import lombok.Data;

@Data
public class CreateProjectRequest {
    private String projectName;
    private String description;
}