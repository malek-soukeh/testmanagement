package com.example.testmanagement.Requests;
import lombok.Data;
@Data
    public class TestRequest {
        private String testType;
        private int threads;
        private int rampup;
        private int loops;
    }

