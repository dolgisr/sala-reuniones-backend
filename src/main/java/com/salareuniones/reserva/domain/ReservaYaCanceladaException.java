package com.salareuniones.reserva.domain;

import com.salareuniones.shared.domain.ConflictoException;

public class ReservaYaCanceladaException extends ConflictoException {

    public ReservaYaCanceladaException() {
        super("La reserva ya está cancelada");
    }
}
