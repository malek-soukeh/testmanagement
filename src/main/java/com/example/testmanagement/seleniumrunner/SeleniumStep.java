package com.example.testmanagement.seleniumrunner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class SeleniumStep {
        private String stepName;
        private String actionType;
        private String actionTarget;
        private String actionValue;
        private String expectedResult;
    }

