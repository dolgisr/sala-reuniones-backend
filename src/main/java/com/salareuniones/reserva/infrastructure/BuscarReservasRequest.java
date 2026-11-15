package com.salareuniones.reserva.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record BuscarReservasRequest(
        @NotNull(message = "La sala es obligatoria")
        @Schema(example = "6f37deab-3688-4735-8fae-1a219edcb227")
        UUID salaId,

        @NotNull(message = "La fecha es obligatoria")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(example = "2026-11-16")
        LocalDate fecha
) {
}
