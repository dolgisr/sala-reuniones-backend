package com.salareuniones.reserva.infrastructure.http;

import com.salareuniones.reserva.domain.Reserva;
import com.salareuniones.shared.infrastructure.http.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper implements BaseMapper<Reserva, ReservaResponse> {

    // Leer el id de la sala no carga la sala: Hibernate lo tiene en el proxy de la relación lazy
    @Override
    public ReservaResponse toDto(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getSala().getId(),
                reserva.getResponsable(),
                reserva.getInicio(),
                reserva.getFin(),
                reserva.getEstado());
    }
}
