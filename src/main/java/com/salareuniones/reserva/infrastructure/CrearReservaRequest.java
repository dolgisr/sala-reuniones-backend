package com.salareuniones.reserva.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record CrearReservaRequest(
        @NotNull(message = "la sala es obligatoria")
        @Schema(example = "6f37deab-3688-4735-8fae-1a219edcb227")
        UUID salaId,

        @NotBlank(message = "el responsable es obligatorio")
        @Size(max = 100, message = "el responsable no puede superar los 100 caracteres")
        @Schema(example = "Ana García")
        String responsable,

        @NotNull(message = "el inicio es obligatorio")
        @Schema(example = "2026-11-16T10:00:00")
        LocalDateTime inicio,

        @NotNull(message = "el fin es obligatorio")
        @Schema(example = "2026-11-16T11:00:00")
        LocalDateTime fin
) {

    @JsonIgnore
    @AssertTrue(message = "el fin debe ser posterior al inicio")
    public boolean isRangoValido() {
        return inicio == null || fin == null || fin.isAfter(inicio);
    }
}
