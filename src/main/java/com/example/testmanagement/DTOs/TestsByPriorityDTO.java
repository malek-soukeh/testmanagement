package com.example.testmanagement.DTOs;

import java.util.List;

public class TestsByPriorityDTO {
    private List<String> labels; // priority names
    private List<Long> data; // counts

    public TestsByPriorityDTO(List<String> labels, List<Long> data) {
        this.labels = labels;
        this.data = data;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Long> getData() {
        return data;
    }

    public void setData(List<Long> data) {
        this.data = data;
    }
}
