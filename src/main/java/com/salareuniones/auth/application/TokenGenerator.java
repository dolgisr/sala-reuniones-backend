package com.salareuniones.auth.application;

public interface TokenGenerator {

    Token generate(String username);
}
