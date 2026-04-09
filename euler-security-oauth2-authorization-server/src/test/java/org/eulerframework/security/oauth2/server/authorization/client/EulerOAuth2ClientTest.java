/*
 * Copyright 2013-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eulerframework.security.oauth2.server.authorization.client;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.util.Base64URL;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.jackson.EulerSecurityJsonMapperFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2ClientRegistration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.http.converter.OAuth2ClientRegistrationHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EulerOAuth2ClientTest {

    @Test
    public void test() throws ParseException, JOSEException {
        RegisteredClient registeredClient = RegisteredClient.withId("id")
                .clientId("clientId")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/redirectUri")
                .clientSettings(ClientSettings.builder().jwkSetUrl("https://example.com/jwks")
                        .setting("jwks", new RSAKey.Builder(new Base64URL("n"), new Base64URL("e"))
                                .keyUse(KeyUse.SIGNATURE)
                                .keyID("123")
                                .build())
                        .build())
                .build();
        String json = EulerSecurityJsonMapperFactory.getInstance().writeValueAsString(registeredClient);
        System.out.println(json);

        JWK rsaJwk = new RSAKey.Builder(new Base64URL("n"), new Base64URL("e"))
                .keyUse(KeyUse.SIGNATURE)
                .keyID("123")
                .build();
        String jwkJson = JacksonUtils.writeValueAsString(rsaJwk.toJSONObject());
        System.out.println(rsaJwk.toJSONString());
        System.out.println(jwkJson);
        JWK deserizedJwk = JWK.parse(jwkJson);
        System.out.println(deserizedJwk.getClass());
        System.out.println(deserizedJwk.toJSONString());

        ECKey ecKey = new ECKeyGenerator(Curve.P_256)
                .keyUse(KeyUse.SIGNATURE)
                .keyID("123")
                .generate();
        JWK ecJwk = ecKey.toPublicJWK();
        System.out.println(ecJwk.toJSONString());
    }


    @Test
    public void rfc7591() throws JOSEException, IOException, ParseException {
        // RFC 7591 §3.2.1 Client Registration Response
        // https://datatracker.ietf.org/doc/html/rfc7591#section-3.2.1
        // Complete Client Registration Response with all RFC 7591 standard fields
        // Note: "scope" is a space-separated string per §2, NOT an array
        //       "token_endpoint_auth_method" is a single string per §2, NOT an array
        //       "jwks" and "jwks_uri" are mutually exclusive per §2
        String json = """
                {
                    "client_id": "s6BhdRkqt3",
                    "client_id_issued_at": 1704067200,
                    "client_secret": "cf136dc3c1fc93f31185e5885805d",
                    "client_secret_expires_at": 1735689600,
                    "client_name": "My Example Client",
                    "redirect_uris": [
                        "https://client.example.org/callback",
                        "https://client.example.org/callback2"
                    ],
                    "token_endpoint_auth_method": "client_secret_basic",
                    "grant_types": [
                        "authorization_code",
                        "refresh_token"
                    ],
                    "response_types": [
                        "code"
                    ],
                    "scope": "openid profile email",
                    "jwks": {
                        "keys": [
                            {
                                "kty": "EC",
                                "use": "sig",
                                "crv": "P-256",
                                "kid": "test-key-1",
                                "x": "NRMar-uaHvQ_8vn7G_aOV_7q-aeBMIJlinyahZtT0No",
                                "y": "9APQurDfe1MPFTjEVa0wCNn_hOiHRS6RPgFssvImBeg"
                            }
                        ]
                    }
                }
                """;

        System.out.println(json);

        // Parse via OAuth2ClientRegistrationHttpMessageConverter
        OAuth2ClientRegistrationHttpMessageConverter converter = new OAuth2ClientRegistrationHttpMessageConverter();
        HttpInputMessage inputMessage = createJsonInputMessage(json);
        OAuth2ClientRegistration registration = converter.read(OAuth2ClientRegistration.class, inputMessage);

        // §3.2.1 Response fields
        assertEquals("s6BhdRkqt3", registration.getClientId());
        assertEquals(Instant.ofEpochSecond(1704067200L), registration.getClientIdIssuedAt());
        assertEquals("cf136dc3c1fc93f31185e5885805d", registration.getClientSecret());
        assertEquals(Instant.ofEpochSecond(1735689600L), registration.getClientSecretExpiresAt());

        // §2 Client Metadata fields
        assertEquals("My Example Client", registration.getClientName());
        assertEquals(
                List.of("https://client.example.org/callback", "https://client.example.org/callback2"),
                registration.getRedirectUris());
        assertEquals("client_secret_basic", registration.getTokenEndpointAuthenticationMethod());
        assertEquals(List.of("authorization_code", "refresh_token"), registration.getGrantTypes());
        assertEquals(List.of("code"), registration.getResponseTypes());
        assertEquals(List.of("openid", "profile", "email"), registration.getScopes());

        // §2 jwks - JWK Set document value containing the client's public keys
        @SuppressWarnings("unchecked")
        Map<String, Object> jwksClaim = (Map<String, Object>) registration.getClaims().get("jwks");
        assertNotNull(jwksClaim, "jwks claim should be present");
        JWKSet parsedJwks = JWKSet.parse(jwksClaim);
        assertEquals(1, parsedJwks.getKeys().size());
        JWK parsedKey = parsedJwks.getKeys().get(0);
        assertEquals("test-key-1", parsedKey.getKeyID());
        assertEquals(KeyType.EC, parsedKey.getKeyType());
        assertEquals(KeyUse.SIGNATURE, parsedKey.getKeyUse());
    }

    @Test
    public void rfc7591_clientSecretExpiresAtZero() throws IOException {
        // RFC 7591 §3.2.1: "client_secret_expires_at" value of 0 means it will not expire
        String json = """
                {
                    "client_id": "s6BhdRkqt3",
                    "client_secret": "cf136dc3c1fc93f31185e5885805d",
                    "client_secret_expires_at": 0
                }
                """;

        OAuth2ClientRegistrationHttpMessageConverter converter = new OAuth2ClientRegistrationHttpMessageConverter();
        OAuth2ClientRegistration registration = converter.read(
                OAuth2ClientRegistration.class, createJsonInputMessage(json));

        assertEquals("s6BhdRkqt3", registration.getClientId());
        assertEquals("cf136dc3c1fc93f31185e5885805d", registration.getClientSecret());
        // 0 means "does not expire" — Spring converts this to null
        assertNull(registration.getClientSecretExpiresAt());
    }

    private static HttpInputMessage createJsonInputMessage(String json) {
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }

    @Test
    public void testJwksRoundTrip() throws JOSEException, ParseException {
        // Generate a JWKSet with an EC key
        ECKey ecKey = new ECKeyGenerator(Curve.P_256)
                .keyUse(KeyUse.SIGNATURE)
                .keyID("ec-key-1")
                .generate();
        JWKSet originalJwks = new JWKSet(ecKey.toPublicJWK());

        // Store as Map<String, Object> (the new jwks representation at EulerOAuth2Client level)
        @SuppressWarnings("unchecked")
        Map<String, Object> jwksMap = originalJwks.toJSONObject();
        assertNotNull(jwksMap);

        // Verify JWKSet can be reconstructed from Map
        JWKSet retrievedJwks = JWKSet.parse(jwksMap);
        System.out.println("Original:  " + originalJwks.toJSONObject());
        System.out.println("Retrieved: " + retrievedJwks.toJSONObject());
        assertEquals(1, retrievedJwks.getKeys().size());
        assertEquals("ec-key-1", retrievedJwks.getKeys().get(0).getKeyID());

        // Simulate JSON round-trip: Map -> JSON string -> Map -> JWKSet
        String json = JacksonUtils.writeValueAsString(jwksMap);
        System.out.println("Serialized JSON: " + json);

        @SuppressWarnings("unchecked")
        Map<String, Object> parsedMap = JacksonUtils.readValue(json, Map.class);
        JWKSet restoredJwks = JWKSet.parse(parsedMap);

        // Verify jwks survives JSON round-trip
        System.out.println("Restored:  " + restoredJwks.toJSONObject());
        assertEquals(1, restoredJwks.getKeys().size());
        assertEquals("ec-key-1", restoredJwks.getKeys().get(0).getKeyID());
    }

}