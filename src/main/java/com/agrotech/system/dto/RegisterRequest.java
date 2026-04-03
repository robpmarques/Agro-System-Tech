package com.agrotech.system.dto;

import com.agrotech.system.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        Role role
) {
}
