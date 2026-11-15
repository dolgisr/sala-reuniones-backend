package com.salareuniones.reserva.application;

import com.salareuniones.reserva.domain.EstadoReserva;
import com.salareuniones.reserva.domain.Reserva;
import com.salareuniones.reserva.domain.ReservaRepository;
import com.salareuniones.reserva.domain.ReservaSolapadaException;
import com.salareuniones.sala.domain.SalaRepository;
import com.salareuniones.shared.application.BaseService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaService extends BaseService<Reserva, ReservaRepository> {

    private final SalaRepository salaRepository;

    public ReservaService(ReservaRepository repository, SalaRepository salaRepository) {
        super(repository, "Reserva");
        this.salaRepository = salaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Reserva> buscar(UUID salaId, LocalDate fecha, Pageable pageable) {
        return repository.buscarPorSalaEnRango(salaId, fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(), pageable);
    }

    @Transactional
    public void cancelar(UUID id) {
        Reserva reserva = findById(id);
        reserva.cancelar();
        save(reserva);
    }

    @Override
    protected void validateCreate(Reserva reserva) {
        UUID salaId = reserva.getSala().getId();
        // Bloquea la sala para que dos reservas simultáneas no pasen ambas la comprobación
        salaRepository.findByIdForUpdate(salaId);
        if (repository.existeSolapamiento(salaId, reserva.getInicio(), reserva.getFin(), EstadoReserva.ACTIVA)) {
            throw new ReservaSolapadaException();
        }
    }
}
