package com.salareuniones.reserva.domain;

import com.salareuniones.shared.domain.ConflictoException;

public class ReservaSolapadaException extends ConflictoException {

    public ReservaSolapadaException() {
        super("La sala ya está reservada en ese horario");
    }
}
