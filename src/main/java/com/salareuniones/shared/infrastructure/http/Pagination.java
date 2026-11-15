package com.salareuniones.shared.infrastructure.http;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Pagination {

    private Pagination() {
    }

    /** Conserva la página y el tamaño pedidos, pero fija el orden: el cliente no elige por qué campo ordenar. */
    public static Pageable withSort(Pageable pageable, String... properties) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(properties));
    }
}
