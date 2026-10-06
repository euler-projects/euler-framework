/*
 * Copyright 2013-present the original author or authors.
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

package org.eulerframework.security.web.authentication.appattest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.eulerframework.security.authentication.appattest.AppAttestInstanceKeyRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestInstanceKeyRegistrationAuthenticationToken;
import org.eulerframework.security.authentication.appattest.InvalidInstanceKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the endpoint contract of {@link AppAttestInstanceKeyRegistrationEndpointFilter}: which
 * requests it answers, what a registration returns, and how the two distinct kinds of
 * failure are told apart &mdash; a request the server could not use is a {@code 400}, while
 * a credential that did not verify is a {@code 401}.
 */
class AppAttestInstanceKeyRegistrationEndpointFilterTest {

    private static final String ENDPOINT = "/app_attest/keys";
    private static final String KID = "attest-kid-1";
    private static final String CHALLENGE = "challenge-1";
    private static final String ASSERTION = "assertion-1";

    /**
     * A body the converter forwards verbatim. Deciding whether it is a registrable key is the
     * provider's job, and nimbus is deliberately not on this module's classpath.
     */
    private static final String SUBMITTED_JWK =
            "{\"kty\":\"EC\",\"crv\":\"P-256\",\"x\":\"f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU\","
                    + "\"y\":\"x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0\",\"alg\":\"ES256\"}";

    @Test
    void registersAKeyAndReturnsItWithItsServerDerivedKid() throws Exception {
        AppAttestInstanceKeyRegistration stored = new AppAttestInstanceKeyRegistration(KID, "thumbprint-1",
                "{\"kty\":\"EC\",\"kid\":\"thumbprint-1\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter(authentication -> AppAttestInstanceKeyRegistrationAuthenticationToken.registered(stored))
                .doFilter(request(headers(), SUBMITTED_JWK), response, new RecordingFilterChain());

        assertEquals(201, response.getStatus());
        // The response is the stored JWK verbatim: it already carries the derived kid, so a
        // client can quote it straight back in an assertion header.
        assertEquals(stored.jwk(), response.getContentAsString());
        assertTrue(response.getContentType().startsWith("application/json"));
    }

    @Test
    void incompleteCredentialIsABadRequest() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        // No App-Attest-Kid: an assertion carries no credential ID, so the server cannot
        // locate the KEY it was made with.
        filter(recordingProvider())
                .doFilter(request(headersWithoutKid(), "{}"), response, new RecordingFilterChain());

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("invalid_request"));
        assertTrue(response.getContentAsString().contains(AppAttestParameterNames.HEADER_KID));
    }

    @Test
    void emptyBodyIsABadRequest() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter(recordingProvider())
                .doFilter(request(headers(), ""), response, new RecordingFilterChain());

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("invalid_request"));
    }

    @Test
    void anUnusableKeyIsABadRequestNotAnAuthenticationFailure() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        // The credential was fine; what the caller asked to register was not. Reporting this
        // as 401 would send the client to re-fetch a challenge for a body it must rewrite.
        filter(authentication -> {
            throw new InvalidInstanceKeyException("The JWK cannot be registered: unsupported key type");
        }).doFilter(request(headers(), "{\"kty\":\"oct\"}"), response, new RecordingFilterChain());

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("invalid_request"));
        assertTrue(response.getContentAsString().contains("unsupported key type"));
    }

    @Test
    void aCredentialThatDoesNotVerifyIsUnauthorized() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter(authentication -> {
            throw new BadCredentialsException("Invalid or expired challenge");
        }).doFilter(request(headers(), SUBMITTED_JWK), response, new RecordingFilterChain());

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("key_registration_failed"));
        // The reason travels to the client, so an actionable message from the provider is not
        // swallowed by the mapping.
        assertTrue(response.getContentAsString().contains("Invalid or expired challenge"));
    }

    @Test
    void nonMatchingPathPassesThrough() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter(recordingProvider())
                .doFilter(new MockHttpServletRequest("POST", "/app_attest/register"), response, chain);

        assertTrue(chain.called);
        assertEquals("", response.getContentAsString());
    }

    @Test
    void nonPostPassesThrough() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter(recordingProvider()).doFilter(new MockHttpServletRequest("GET", ENDPOINT), response, chain);

        assertTrue(chain.called);
    }

    /**
     * The body has to be read before the credential is verified, so an unbounded read would
     * let any caller who can supply three headers make the server buffer whatever it liked
     * for a request that is going to be refused anyway. The bound is answered as the request
     * error it is, and the provider is never reached.
     */
    @Test
    void anOversizedBodyIsABadRequestThatNeverReachesTheProvider() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String oversized = "{\"jwk\":\"" + "x".repeat(
                AppAttestInstanceKeyRegistrationAuthenticationConverter.MAX_REQUEST_BODY_SIZE) + "\"}";

        filter(recordingProvider())
                .doFilter(request(headers(), oversized), response, new RecordingFilterChain());

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("invalid_request"));
        assertTrue(response.getContentAsString().contains("exceeds"));
    }

    /** The bound refuses a body that could not be a key, not the largest one that could. */
    @Test
    void aBodyExactlyAtTheLimitIsStillAccepted() throws Exception {
        AppAttestInstanceKeyRegistration stored = new AppAttestInstanceKeyRegistration(KID, "thumbprint-1", "{}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter(authentication -> AppAttestInstanceKeyRegistrationAuthenticationToken.registered(stored))
                .doFilter(request(headers(), padToLimit(SUBMITTED_JWK)), response, new RecordingFilterChain());

        assertEquals(201, response.getStatus());
    }

    // ---- helpers ----

    /**
     * Pad a body out to exactly the limit: the largest one the endpoint must still take, and
     * the boundary against which the refusal above is meaningful.
     */
    private static String padToLimit(String body) {
        int limit = AppAttestInstanceKeyRegistrationAuthenticationConverter.MAX_REQUEST_BODY_SIZE;
        String prefix = body.substring(0, body.length() - 1) + ",\"pad\":\"";
        int filler = limit - prefix.length() - 2;   // the closing quote and brace
        assertTrue(filler > 0, "the fixture body must leave room to pad up to the limit");
        return prefix + "p".repeat(filler) + "\"}";
    }

    private static AppAttestInstanceKeyRegistrationEndpointFilter filter(StubProvider provider) {
        return new AppAttestInstanceKeyRegistrationEndpointFilter(
                new AppAttestInstanceKeyRegistrationAuthenticationConverter(), provider, ENDPOINT);
    }

    /** A provider that never runs, for the cases the filter has to answer on its own. */
    private static StubProvider recordingProvider() {
        return authentication -> {
            throw new AssertionError("the filter must answer this request without reaching the provider");
        };
    }

    private static Map<String, String> headers() {
        return Map.of(
                AppAttestParameterNames.HEADER_KID, KID,
                AppAttestParameterNames.HEADER_CHALLENGE, CHALLENGE,
                AppAttestParameterNames.HEADER_ASSERTION, ASSERTION);
    }

    private static Map<String, String> headersWithoutKid() {
        return Map.of(
                AppAttestParameterNames.HEADER_CHALLENGE, CHALLENGE,
                AppAttestParameterNames.HEADER_ASSERTION, ASSERTION);
    }

    private static MockHttpServletRequest request(Map<String, String> headers, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ENDPOINT);
        request.setContentType("application/json");
        headers.forEach(request::addHeader);
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    /** Stands in for the provider, which is covered by its own test in the core module. */
    interface StubProvider extends AuthenticationProvider {

        @Override
        default boolean supports(Class<?> authentication) {
            return AppAttestInstanceKeyRegistrationAuthenticationToken.class.isAssignableFrom(authentication);
        }
    }

    static class RecordingFilterChain implements FilterChain {

        boolean called;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            this.called = true;
        }
    }
}
