package com.salareuniones.shared.infrastructure.http;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginatedResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PaginatedResponse<T> of(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
