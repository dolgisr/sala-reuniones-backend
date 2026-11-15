package com.salareuniones.sala.infrastructure;

import com.salareuniones.sala.application.SalaService;
import com.salareuniones.sala.domain.Sala;
import com.salareuniones.shared.infrastructure.http.OpenApiConfig;
import com.salareuniones.shared.infrastructure.http.Pagination;
import com.salareuniones.shared.infrastructure.http.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Salas")
@RestController
@RequestMapping("/api/salas")
public class SalaController {

    private final SalaService salaService;
    private final SalaMapper salaMapper;

    public SalaController(SalaService salaService, SalaMapper salaMapper) {
        this.salaService = salaService;
        this.salaMapper = salaMapper;
    }

    @Operation(summary = "Lista las salas de forma paginada")
    @GetMapping
    public PaginatedResponse<SalaResponse> listar(
            @ParameterObject @PageableDefault(size = 20)
            Pageable pageable) {
        return PaginatedResponse.of(salaService.findAll(Pagination.withSort(pageable, "nombre")).map(salaMapper::toDto));
    }

    @Operation(summary = "Crea una sala")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void crear(@Valid @RequestBody CrearSalaRequest request) {
        salaService.create(Sala.create(request.nombre()));
    }
}
