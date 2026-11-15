package com.salareuniones.reserva.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.salareuniones.reserva.domain.EstadoReserva;
import com.salareuniones.reserva.domain.Reserva;
import com.salareuniones.reserva.domain.ReservaRepository;
import com.salareuniones.reserva.domain.errors.ReservaSolapadaException;
import com.salareuniones.sala.application.SalaService;
import com.salareuniones.sala.domain.Sala;
import com.salareuniones.shared.domain.errors.EntidadNoEncontradaException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository repository;

    @Mock
    private SalaService salaService;

    private ReservaService service;

    private final Sala sala = Sala.create("Sala Azul");
    private final LocalDateTime inicio = LocalDateTime.of(2030, 1, 10, 10, 0);
    private final LocalDateTime fin = inicio.plusHours(1);

    @BeforeEach
    void setUp() {
        service = new ReservaService(repository, salaService);
    }

    @Test
    void crearConSolapamientoLanzaConflictoYNoGuarda() {
        Reserva reserva = Reserva.create(sala, "Ana", inicio, fin);
        when(repository.existeSolapamiento(sala.getId(), inicio, fin, EstadoReserva.ACTIVA)).thenReturn(true);

        assertThatThrownBy(() -> service.create(reserva)).isInstanceOf(ReservaSolapadaException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void crearSinSolapamientoBloqueaLaSalaAntesDeComprobarYGuarda() {
        Reserva reserva = Reserva.create(sala, "Ana", inicio, fin);

        service.create(reserva);

        // El bloqueo debe ir antes de la comprobación: si no, dos peticiones simultáneas pasarían las dos
        InOrder orden = inOrder(salaService, repository);
        orden.verify(salaService).lockById(sala.getId());
        orden.verify(repository).existeSolapamiento(sala.getId(), inicio, fin, EstadoReserva.ACTIVA);
        orden.verify(repository).save(reserva);
    }

    @Test
    void cancelarUnaReservaInexistenteLanzaNoEncontrada() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdForUpdate(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(id))
                .isInstanceOf(EntidadNoEncontradaException.class)
                .hasMessageContaining("Reserva");
    }
}
