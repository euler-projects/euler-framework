package org.eulerframework.data.file.web.security;

import io.jsonwebtoken.*;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


public class JwtFileTokenRegistry implements FileTokenRegistry {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtFileTokenRegistry() {
        KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    @Override
    public FileToken generateToken(String fileId) {
        Instant isa = Instant.now();
        Instant exp = isa.plus(1, ChronoUnit.HOURS);

        Claims claims = Jwts.claims()
                .subject("anonymous")
                .issuedAt(Date.from(isa))
                .expiration(Date.from(exp))
                .add(JwtFileToken.FILE_ID_CLAIM, fileId)
                .build();


        String jwt = Jwts.builder()
                .claims(claims)
                .signWith(privateKey)
                .compact();

        return new JwtFileToken(jwt, claims);
    }

    @Override
    public FileToken getTokenByTokenValue(String tokenValue) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(tokenValue);

        Claims claims = jws.getPayload();

        return new JwtFileToken(tokenValue, claims);
    }
}
