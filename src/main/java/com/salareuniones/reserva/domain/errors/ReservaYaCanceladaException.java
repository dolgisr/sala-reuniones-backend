package com.salareuniones.reserva.domain.errors;

import com.salareuniones.shared.domain.errors.ConflictoException;

public class ReservaYaCanceladaException extends ConflictoException {

    public ReservaYaCanceladaException() {
        super("La reserva ya está cancelada");
    }
}
