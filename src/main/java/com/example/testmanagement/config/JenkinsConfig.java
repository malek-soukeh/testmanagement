package com.example.testmanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour Jenkins
 */
@Configuration
@ConfigurationProperties(prefix = "jenkins")
@Data
public class JenkinsConfig {
    private String jobUrl;
    private String user;
    private String token;
    private String callbackToken;

}

