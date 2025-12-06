package com.example.testmanagement.DTOs;

import java.time.LocalDateTime;

public class ActivityDTO {
    private String type; // pass, fail, info, progress
    private String message;
    private LocalDateTime time;
    private String icon;

    public ActivityDTO(String type, String message, LocalDateTime time, String icon) {
        this.type = type;
        this.message = message;
        this.time = time;
        this.icon = icon;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
