package com.salareuniones;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class ReservasIntegrationTest extends AbstractIntegrationTest {

    @Test
    void loginConCredencialesIncorrectasDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "test", "password": "mala"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void consultarEsLibrePeroCrearYCancelarExigenToken() throws Exception {
        mvc.perform(get("/api/salas")).andExpect(status().isOk());

        mvc.perform(post("/api/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Sala sin token"}
                                """))
                .andExpect(status().isUnauthorized());

        mvc.perform(delete("/api/reservas/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reservarDevuelve409SiLaSalaEstaOcupadaYRespetaLosBordes() throws Exception {
        String salaId = crearSala("Sala Solapamiento");

        reservar(salaId, "2030-01-10T10:00:00", "2030-01-10T11:00:00").andExpect(status().isCreated());

        // Se solapa en parte
        reservar(salaId, "2030-01-10T10:30:00", "2030-01-10T11:30:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La sala ya está reservada en ese horario"));

        // Contiene la reserva existente
        reservar(salaId, "2030-01-10T09:00:00", "2030-01-10T12:00:00").andExpect(status().isConflict());

        // Empieza justo cuando termina la otra: no hay solapamiento
        reservar(salaId, "2030-01-10T11:00:00", "2030-01-10T12:00:00").andExpect(status().isCreated());

        // Mismo horario en otra sala: libre
        String otraSala = crearSala("Sala Solapamiento 2");
        reservar(otraSala, "2030-01-10T10:00:00", "2030-01-10T11:00:00").andExpect(status().isCreated());
    }

    @Test
    void datosInvalidosDevuelven400ConElErrorDeCadaCampo() throws Exception {
        String salaId = crearSala("Sala Validación");

        reservar(salaId, "2030-01-10T11:00:00", "2030-01-10T10:00:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rangoValido").value("El fin debe ser posterior al inicio"));

        mvc.perform(post("/api/reservas")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"salaId": "no-es-un-uuid", "responsable": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.salaId").value("El formato no es válido"));

        reservar("00000000-0000-0000-0000-000000000000", "2030-01-10T10:00:00", "2030-01-10T11:00:00")
                .andExpect(status().isNotFound());
    }

    @Test
    void listarFiltraPorSalaYFechaConPaginacion() throws Exception {
        String salaId = crearSala("Sala Listado");
        reservar(salaId, "2030-02-01T09:00:00", "2030-02-01T10:00:00").andExpect(status().isCreated());
        reservar(salaId, "2030-02-01T12:00:00", "2030-02-01T13:00:00").andExpect(status().isCreated());
        // Cruza la medianoche: debe aparecer en los dos días
        reservar(salaId, "2030-02-01T23:00:00", "2030-02-02T01:00:00").andExpect(status().isCreated());
        reservar(salaId, "2030-02-03T10:00:00", "2030-02-03T11:00:00").andExpect(status().isCreated());

        // Sin token: la consulta es pública
        mvc.perform(get("/api/reservas").param("salaId", salaId).param("fecha", "2030-02-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.data[*].inicio").value(contains(
                        "2030-02-01T09:00:00", "2030-02-01T12:00:00", "2030-02-01T23:00:00")));

        mvc.perform(get("/api/reservas").param("salaId", salaId).param("fecha", "2030-02-02"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.data[0].inicio").value("2030-02-01T23:00:00"));

        mvc.perform(get("/api/reservas").param("salaId", salaId).param("fecha", "2030-02-01")
                        .param("page", "1").param("size", "2"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.data.length()").value(1));

        mvc.perform(get("/api/reservas").param("salaId", salaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fecha").value("La fecha es obligatoria"));
    }

    @Test
    void cancelarLiberaElHorarioYNoSePuedeCancelarDosVeces() throws Exception {
        String salaId = crearSala("Sala Cancelación");
        reservar(salaId, "2030-03-01T10:00:00", "2030-03-01T11:00:00").andExpect(status().isCreated());
        String reservaId = idDeLaPrimeraReserva(salaId, "2030-03-01");

        mvc.perform(delete("/api/reservas/{id}", reservaId).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNoContent());

        mvc.perform(delete("/api/reservas/{id}", reservaId).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La reserva ya está cancelada"));

        // La cancelada sigue en el listado, y su horario vuelve a estar libre.
        // Las dos empiezan a la misma hora y se desempatan por id (aleatorio): no se comprueba el orden
        reservar(salaId, "2030-03-01T10:00:00", "2030-03-01T11:00:00").andExpect(status().isCreated());
        mvc.perform(get("/api/reservas").param("salaId", salaId).param("fecha", "2030-03-01"))
                .andExpect(jsonPath("$.data[*].estado").value(containsInAnyOrder("ACTIVA", "CANCELADA")));
    }

    private ResultActions reservar(String salaId, String inicio, String fin) throws Exception {
        return mvc.perform(post("/api/reservas")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"salaId": "%s", "responsable": "Ana", "inicio": "%s", "fin": "%s"}
                        """.formatted(salaId, inicio, fin)));
    }

    private String idDeLaPrimeraReserva(String salaId, String fecha) throws Exception {
        String body = mvc.perform(get("/api/reservas").param("salaId", salaId).param("fecha", fecha))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data[0].id");
    }
}
