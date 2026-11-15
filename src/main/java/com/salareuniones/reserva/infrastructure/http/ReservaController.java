package com.salareuniones.reserva.infrastructure.http;

import com.salareuniones.reserva.application.ReservaService;
import com.salareuniones.reserva.domain.Reserva;
import com.salareuniones.sala.application.SalaService;
import com.salareuniones.sala.domain.Sala;
import com.salareuniones.shared.infrastructure.http.OpenApiConfig;
import com.salareuniones.shared.infrastructure.http.Pagination;
import com.salareuniones.shared.infrastructure.http.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reservas")
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final SalaService salaService;
    private final ReservaMapper reservaMapper;

    public ReservaController(ReservaService reservaService, SalaService salaService, ReservaMapper reservaMapper) {
        this.reservaService = reservaService;
        this.salaService = salaService;
        this.reservaMapper = reservaMapper;
    }

    @Operation(summary = "Lista las reservas de una sala en una fecha")
    @GetMapping
    public PaginatedResponse<ReservaResponse> listar(
            @ParameterObject @Valid BuscarReservasRequest request,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        // "id" desempata reservas que empiezan a la misma hora, para que el orden sea estable entre páginas
        Pageable porInicio = Pagination.withSort(pageable, "inicio", "id");
        return PaginatedResponse.of(
                reservaService.buscar(request.salaId(), request.fecha(), porInicio).map(reservaMapper::toDto));
    }

    @Operation(summary = "Crea una reserva; 409 si la sala ya está ocupada en ese horario")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void crear(@Valid @RequestBody CrearReservaRequest request) {
        Sala sala = salaService.findById(request.salaId());
        reservaService.create(Reserva.create(sala, request.responsable(), request.inicio(), request.fin()));
    }

    @Operation(summary = "Cancela una reserva; el horario queda libre. 409 si ya estaba cancelada")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable UUID id) {
        reservaService.cancelar(id);
    }
}
