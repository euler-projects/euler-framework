package org.eulerframework.data.file.web.security;

import io.jsonwebtoken.Claims;

import java.time.Instant;

public class JwtFileToken implements FileToken {
    static final String FILE_ID_CLAIM = "file_id";

    private final String tokenValue;
    private final Claims claims;

    public JwtFileToken(String tokenValue, Claims claims) {
        this.tokenValue = tokenValue;
        this.claims = claims;
    }

    @Override
    public String getFileId() {
        return this.claims.get(FILE_ID_CLAIM, String.class);
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
