package com.salareuniones.sala.domain.errors;

import com.salareuniones.shared.domain.errors.ConflictoException;

public class SalaDuplicadaException extends ConflictoException {

    public SalaDuplicadaException(String nombre) {
        super("Ya existe una sala con el nombre '" + nombre + "'");
    }
}
