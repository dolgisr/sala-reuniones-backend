package com.salareuniones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base de los tests de integración: la API completa contra un PostgreSQL real en Docker, la misma base
 * que usa la aplicación, incluidos el tipo enum, el índice parcial y el SELECT ... FOR UPDATE.
 *
 * <p>El contenedor se arranca una sola vez y lo comparten todas las clases que heredan de aquí,
 * igual que Spring comparte el contexto entre ellas. Cada test usa nombres de sala propios.
 */
@SpringBootTest(properties = {
        "app.security.username=test",
        "app.security.password=test-password"
})
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected MockMvc mvc;

    private String token;

    @BeforeEach
    void login() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "test", "password": "test-password"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(body, "$.accessToken");
    }

    protected String bearer() {
        return "Bearer " + token;
    }

    protected ResultActions postSala(String jsonBody) throws Exception {
        return mvc.perform(post("/api/salas")
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));
    }

    /** Crea la sala y devuelve su id. Crear no devuelve cuerpo, así que el id se obtiene del listado. */
    protected String crearSala(String nombre) throws Exception {
        postSala("{\"nombre\": \"" + nombre + "\"}").andExpect(status().isCreated());

        String body = mvc.perform(get("/api/salas").param("size", "100"))
                .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(body, "$.data[?(@.nombre == '" + nombre + "')].id");
        assertThat(ids).hasSize(1);
        return ids.get(0);
    }
}
