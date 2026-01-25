package org.eulerframework.data.file.web.security;

import io.jsonwebtoken.Claims;

import java.time.Instant;

public class JwtFileToken implements FileToken {
    private final String tokenValue;
    private final Claims claims;

    public JwtFileToken(String tokenValue, Claims claims) {
        this.tokenValue = tokenValue;
        this.claims = claims;
    }

    @Override
    public String getFileId() {
        return this.claims.getSubject();
    }

    @Override
    public String getTokenValue() {
        return this.tokenValue;
    }

    @Override
    public Instant getIssuedAt() {
        return this.claims.getIssuedAt().toInstant();
    }

    @Override
    public Instant getExpiresAt() {
        return this.claims.getExpiration().toInstant();
    }
}
