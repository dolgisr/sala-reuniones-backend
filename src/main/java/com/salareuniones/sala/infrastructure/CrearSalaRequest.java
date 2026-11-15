package com.salareuniones.sala.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearSalaRequest(
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 100, message = "el nombre no puede superar los 100 caracteres")
        @Schema(example = "Sala 1")
        String nombre
) {
}
