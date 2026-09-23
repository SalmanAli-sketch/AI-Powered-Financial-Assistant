package com.fintrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private String name;
    private String role;

    public JwtResponse(String token, String email, String name, String role) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
