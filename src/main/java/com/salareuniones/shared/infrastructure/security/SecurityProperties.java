package com.salareuniones.shared.infrastructure.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// Se valida al arrancar: con una configuración incorrecta la aplicación no llega a levantar
@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull @Size(min = 32, message = "Debe tener al menos 32 caracteres para firmar con HS256") String jwtSecret,
        @NotNull Duration jwtExpiration
) {
}
