package com.salareuniones.auth.infrastructure.http;

import com.salareuniones.auth.application.AuthService;
import com.salareuniones.auth.application.Token;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Devuelve un JWT para usar en los endpoints que crean o cancelan")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Token token = authService.login(request.username(), request.password());
        return new TokenResponse(token.value(), "Bearer", token.expiresInSeconds());
    }
}
