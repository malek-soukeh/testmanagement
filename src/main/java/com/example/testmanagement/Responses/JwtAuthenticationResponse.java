package com.example.testmanagement.Responses;

import com.example.testmanagement.Entities.Role;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;

}
