package com.salareuniones.reserva.infrastructure;

import com.salareuniones.reserva.domain.Reserva;
import com.salareuniones.shared.infrastructure.http.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper implements BaseMapper<Reserva, ReservaDto> {

    // Leer el id de la sala no carga la sala: Hibernate lo tiene en el proxy de la relación lazy
    @Override
    public ReservaDto toDto(Reserva reserva) {
        return new ReservaDto(
                reserva.getId(),
                reserva.getSala().getId(),
                reserva.getResponsable(),
                reserva.getInicio(),
                reserva.getFin(),
                reserva.getEstado());
    }
}
