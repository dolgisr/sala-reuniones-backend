package com.salareuniones.reserva.infrastructure;

import com.salareuniones.reserva.domain.EstadoReserva;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservaResponse(
        UUID id,
        UUID salaId,
        String responsable,
        LocalDateTime inicio,
        LocalDateTime fin,
        EstadoReserva estado
) {
}
