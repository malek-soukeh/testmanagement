package com.example.testmanagement.seleniumrunner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeleniumScenario {
    private Long testCaseId;
    private String title;
    private String url;
    private List<SeleniumStep> steps;
}