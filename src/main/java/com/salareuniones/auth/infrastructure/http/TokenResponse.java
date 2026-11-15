package com.salareuniones.auth.infrastructure.http;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
