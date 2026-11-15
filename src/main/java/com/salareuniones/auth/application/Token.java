package com.salareuniones.auth.application;

public record Token(String value, long expiresInSeconds) {
}
