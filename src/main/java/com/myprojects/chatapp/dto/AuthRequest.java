package com.myprojects.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "Username is required")
    private String username;

    private String email;

    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;
}