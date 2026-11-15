package com.salareuniones.shared.infrastructure.http;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /** Nombre del esquema que referencian los endpoints protegidos con @SecurityRequirement. */
    public static final String BEARER = "bearer";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sala Reuniones API")
                        .description("API REST para reservar salas de reuniones. "
                                + "Para crear o cancelar: POST /api/auth/login y pulsar Authorize con el token.")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
