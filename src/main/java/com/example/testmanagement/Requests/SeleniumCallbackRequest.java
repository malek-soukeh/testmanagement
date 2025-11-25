package com.example.testmanagement.Requests;

import lombok.Data;

@Data
public class SeleniumCallbackRequest {
    private String status;
    private String summaryJson;
    private String artifactUrl;
}


