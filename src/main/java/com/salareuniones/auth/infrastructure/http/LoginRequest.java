package com.salareuniones.auth.infrastructure.http;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Schema(example = "admin")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Schema(example = "admin123")
        String password
) {
}
