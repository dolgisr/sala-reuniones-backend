package com.salareuniones.sala.domain;

import com.salareuniones.shared.domain.ConflictoException;

public class SalaDuplicadaException extends ConflictoException {

    public SalaDuplicadaException(String nombre) {
        super("Ya existe una sala con el nombre '" + nombre + "'");
    }
}
