package com.salareuniones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class SalasIntegrationTest extends AbstractIntegrationTest {

    @Test
    void crearUnaSalaDevuelve201SinCuerpoYApareceEnElListado() throws Exception {
        postSala("""
                {"nombre": "Sala Creada"}
                """)
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        mvc.perform(get("/api/salas").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.nombre == 'Sala Creada')].id").isNotEmpty());
    }

    @Test
    void crearSinTokenDevuelve401YNoCreaLaSala() throws Exception {
        mvc.perform(post("/api/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Sala Sin Token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Se requiere un token válido"));

        mvc.perform(get("/api/salas").param("size", "100"))
                .andExpect(jsonPath("$.data[?(@.nombre == 'Sala Sin Token')]").isEmpty());
    }

    @Test
    void unNombreRepetidoDevuelve409AunqueCambienLasMayusculas() throws Exception {
        crearSala("Sala Duplicada");

        postSala("""
                {"nombre": "SALA duplicada"}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe una sala con el nombre 'SALA duplicada'"));
    }

    @Test
    void unNombreInvalidoDevuelve400ConElErrorDelCampo() throws Exception {
        postSala("""
                {"nombre": "   "}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La petición tiene campos inválidos"))
                .andExpect(jsonPath("$.errors.nombre").value("El nombre es obligatorio"));

        postSala("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").value("El nombre es obligatorio"));

        postSala("{\"nombre\": \"" + "a".repeat(101) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").value("El nombre no puede superar los 100 caracteres"));

        postSala("""
                {"nombre": {"texto": "Sala"}}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").value("El formato no es válido"));

        postSala("{\"nombre\":")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la petición no es un JSON válido"));
    }

    @Test
    void elListadoEsPaginadoYSiempreOrdenadoPorNombre() throws Exception {
        crearSala("Sala Orden C");
        crearSala("Sala Orden A");
        crearSala("Sala Orden B");

        // Aunque el cliente pida otro orden, se ordena por nombre
        String body = mvc.perform(get("/api/salas").param("size", "100").param("sort", "nombre,desc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<String> nombres = JsonPath.read(body, "$.data[?(@.nombre =~ /Sala Orden .*/)].nombre");
        assertThat(nombres).containsExactly("Sala Orden A", "Sala Orden B", "Sala Orden C");

        mvc.perform(get("/api/salas").param("page", "0").param("size", "2"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.data.length()").value(2));

        // Máximo 100 por página
        mvc.perform(get("/api/salas").param("size", "500"))
                .andExpect(jsonPath("$.size").value(100));
    }
}
