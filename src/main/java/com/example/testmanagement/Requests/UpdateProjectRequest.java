package com.example.testmanagement.Requests;

import lombok.Data;

@Data
public class UpdateProjectRequest {
    private String projectName;
    private String description;
}