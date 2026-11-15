package com.salareuniones.sala.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.salareuniones.sala.domain.Sala;
import com.salareuniones.sala.domain.SalaRepository;
import com.salareuniones.sala.domain.errors.SalaDuplicadaException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    @Mock
    private SalaRepository repository;

    private SalaService service;

    @BeforeEach
    void setUp() {
        service = new SalaService(repository);
    }

    @Test
    void crearUnaSalaConNombreRepetidoLanzaConflictoYNoGuarda() {
        when(repository.findByNombreIgnoreCase("Sala Azul")).thenReturn(Optional.of(Sala.create("sala azul")));

        assertThatThrownBy(() -> service.create(Sala.create("Sala Azul")))
                .isInstanceOf(SalaDuplicadaException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void crearUnaSalaNuevaLaGuarda() {
        Sala sala = Sala.create("Sala Azul");
        when(repository.findByNombreIgnoreCase("Sala Azul")).thenReturn(Optional.empty());

        service.create(sala);

        verify(repository).save(sala);
    }
}
