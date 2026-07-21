package com.notegather.admin.application.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 64, message = "Username length must be between 3 and 64")
    private String username;

    @Valid
    @NotNull(message = "Password envelope must not be null")
    private PasswordEnvelopeRequest passwordEnvelope;

    @Size(max = 64, message = "Nickname must not exceed 64 characters")
    private String nickname;
}
