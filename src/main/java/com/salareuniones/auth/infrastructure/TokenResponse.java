package com.salareuniones.auth.infrastructure;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
