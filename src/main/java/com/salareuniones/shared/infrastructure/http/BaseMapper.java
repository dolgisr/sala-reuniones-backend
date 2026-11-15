package com.salareuniones.shared.infrastructure.http;

import java.util.List;

public interface BaseMapper<E, D> {

    D toDto(E entity);

    default List<D> toList(List<E> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
