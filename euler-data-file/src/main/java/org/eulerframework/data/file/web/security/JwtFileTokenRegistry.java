package org.eulerframework.data.file.web.security;

import io.jsonwebtoken.*;
import org.eulerframework.common.util.json.JacksonUtils;

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
                .subject(fileId)
                .issuedAt(Date.from(isa))
                .expiration(Date.from(exp))
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

    public static void main(String[] args) {
        JwtFileTokenRegistry jwtFileTokenRegistry = new JwtFileTokenRegistry();
        FileToken fileToken = jwtFileTokenRegistry.generateToken("admin");
        System.out.println("File token: " + JacksonUtils.writeValueAsString(fileToken));
        FileToken varifiedToken = jwtFileTokenRegistry.getTokenByTokenValue(fileToken.getTokenValue());
        System.out.println("Varified Token: " + JacksonUtils.writeValueAsString(varifiedToken));
    }
}
