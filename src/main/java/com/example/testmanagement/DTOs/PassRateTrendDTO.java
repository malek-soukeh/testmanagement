package com.example.testmanagement.DTOs;

import java.util.List;

public class PassRateTrendDTO {
    private List<String> labels; // e.g., "Week 1", "Week 2"
    private List<Double> data; // e.g., 85.0, 90.0

    public PassRateTrendDTO(List<String> labels, List<Double> data) {
        this.labels = labels;
        this.data = data;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }
}
