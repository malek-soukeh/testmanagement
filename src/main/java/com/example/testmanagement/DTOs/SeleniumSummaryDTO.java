package com.example.testmanagement.DTOs;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeleniumSummaryDTO {
    private String title;
    private Boolean overallPassed;
    private List<SeleniumStepSummary> steps = new ArrayList<>();
}


