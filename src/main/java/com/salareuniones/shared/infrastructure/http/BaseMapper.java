package com.salareuniones.shared.infrastructure.http;

public interface BaseMapper<E, D> {

    D toDto(E entity);
}
