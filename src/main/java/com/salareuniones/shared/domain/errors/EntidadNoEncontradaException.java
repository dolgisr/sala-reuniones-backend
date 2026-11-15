package com.salareuniones.shared.domain.errors;

import java.util.UUID;

public class EntidadNoEncontradaException extends RuntimeException {

    public EntidadNoEncontradaException(String nombreEntidad, UUID id) {
        super(nombreEntidad + " con id " + id + " no encontrada");
    }
}
