package com.salareuniones.auth.infrastructure.security;

import com.salareuniones.auth.application.Token;
import com.salareuniones.auth.application.TokenGenerator;
import com.salareuniones.shared.infrastructure.security.SecurityProperties;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** Firma un JWT HS256 con el encoder de Spring Security y la caducidad configurada. */
@Component
public class JwtTokenGenerator implements TokenGenerator {

    private final JwtEncoder jwtEncoder;
    private final SecurityProperties properties;

    public JwtTokenGenerator(JwtEncoder jwtEncoder, SecurityProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    @Override
    public Token generate(String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("sala-reuniones")
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(properties.jwtExpiration()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(value, properties.jwtExpiration().toSeconds());
    }
}
