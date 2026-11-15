package com.salareuniones.reserva.domain.errors;

import com.salareuniones.shared.domain.errors.ConflictoException;

public class ReservaSolapadaException extends ConflictoException {

    public ReservaSolapadaException() {
        super("La sala ya está reservada en ese horario");
    }
}
