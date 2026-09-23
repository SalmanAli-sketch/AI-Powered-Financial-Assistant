package com.fintrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for user login.
 */
@Data
public class LoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 30)
    private String password;
}
