package com.salareuniones.shared.domain.errors;

/** Base de los errores de dominio que chocan con el estado actual (se traducen a 409). */
public abstract class ConflictoException extends RuntimeException {

    protected ConflictoException(String message) {
        super(message);
    }
}
