package com.salareuniones;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

class SeguridadIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JwtEncoder jwtEncoder;

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
    void unTokenCaducadoNoBloqueaLasRutasPublicasPeroNoSirveParaEscribir() throws Exception {
        String caducado = "Bearer " + tokenCaducado();

        // Consultar y volver a hacer login siguen funcionando aunque el cliente envíe el token viejo
        mvc.perform(get("/api/salas").header(HttpHeaders.AUTHORIZATION, caducado))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login")
                        .header(HttpHeaders.AUTHORIZATION, caducado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "test", "password": "test-password"}
                                """))
                .andExpect(status().isOk());

        // Pero no autoriza una escritura
        mvc.perform(post("/api/salas")
                        .header(HttpHeaders.AUTHORIZATION, caducado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Sala con token caducado"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Se requiere un token válido"));
    }

    // Firmado con la clave real de la aplicación, pero caducado hace una hora
    private String tokenCaducado() {
        Instant haceDosHoras = Instant.now().minus(Duration.ofHours(2));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("test")
                .issuedAt(haceDosHoras)
                .expiresAt(haceDosHoras.plus(Duration.ofHours(1)))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
