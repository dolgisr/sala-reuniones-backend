package com.salareuniones.reserva.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.salareuniones.reserva.domain.errors.ReservaYaCanceladaException;
import com.salareuniones.sala.domain.Sala;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ReservaTest {

    private final LocalDateTime inicio = LocalDateTime.of(2030, 1, 10, 10, 0);

    private Reserva nuevaReserva() {
        return Reserva.create(Sala.create("Sala Azul"), "Ana", inicio, inicio.plusHours(1));
    }

    @Test
    void unaReservaNuevaEstaActiva() {
        assertThat(nuevaReserva().getEstado()).isEqualTo(EstadoReserva.ACTIVA);
    }

    @Test
    void cancelarUnaReservaActivaLaDejaCancelada() {
        Reserva reserva = nuevaReserva();

        reserva.cancelar();

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void cancelarDosVecesLanzaErrorDeDominio() {
        Reserva reserva = nuevaReserva();
        reserva.cancelar();

        assertThatThrownBy(reserva::cancelar).isInstanceOf(ReservaYaCanceladaException.class);
    }
}
