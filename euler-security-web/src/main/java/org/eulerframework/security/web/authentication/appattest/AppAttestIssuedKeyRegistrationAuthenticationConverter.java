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

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.authentication.appattest.AppAttestIssuedKeyRegistrationAuthenticationToken;
import org.eulerframework.security.authentication.appattest.InvalidIssuedKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Extracts an issued-key registration request and constructs an
 * {@link AppAttestIssuedKeyRegistrationAuthenticationToken}.
 * <p>
 * The caller is authenticated with an App Attest <b>assertion</b>, not an attestation: the
 * KEY was attested once at App instance registration, and every later call in the App Attest
 * domain re-authenticates with an assertion. Both credential carriages are resolved by
 * {@link AppAttestCredentialResolver} and never mixed, though in practice only the
 * {@code App-Attest-*} headers are usable here &mdash; the request body carries the JWK as
 * JSON, so there are no form parameters to carry a credential in.
 * <p>
 * The key to register is the whole JSON request body, forwarded as submitted. Deciding
 * whether it is a JWK this endpoint may register is the provider's job, which keeps the
 * JWK machinery where it already lives instead of pulling it into the web layer.
 * <p>
 * Returns {@code null} if the assertion credential is incomplete or the body is empty,
 * indicating the request is not an issued-key registration request. A body too large to hold
 * a public JWK is reported as the request error it is rather than buffered.
 */
public class AppAttestIssuedKeyRegistrationAuthenticationConverter implements AuthenticationConverter {

    /**
     * Upper bound on the request body, in bytes.
     * <p>
     * The body is a single public JWK: an EC P-256 key serialises to well under 200 bytes,
     * and even an RSA-4096 public key to under 800, so this sits far above anything that
     * could be registered. It exists because the body has to be read before the credential is
     * verified &mdash; the JWK machinery lives in the provider, not here &mdash; so without a
     * bound any caller who can supply three non-empty headers can make the server buffer an
     * arbitrary amount of memory for a request that will be refused regardless. A servlet
     * container's post-size limit does not cover this: it governs form data, not a raw stream.
     */
    public static final int MAX_REQUEST_BODY_SIZE = 4096;

    @Override
    public Authentication convert(HttpServletRequest request) {
        AppAttestCredential credential = AppAttestCredentialResolver.resolve(request);

        // An assertion's authenticator data carries no credential ID, so the key ID has to be
        // supplied to locate the registered App Attest KEY.
        if (!StringUtils.hasText(credential.assertion())
                || !StringUtils.hasText(credential.challenge())
                || !StringUtils.hasText(credential.kid())) {
            return null;
        }

        String publicKeyJson = readBody(request);
        if (!StringUtils.hasText(publicKeyJson)) {
            return null;
        }

        return AppAttestIssuedKeyRegistrationAuthenticationToken.unauthenticated(
                credential.kid(), credential.challenge(), credential.assertion(), publicKeyJson);
    }

    private static String readBody(HttpServletRequest request) {
        try {
            // One byte past the bound, so exceeding it can be told from exactly filling it.
            byte[] body = request.getInputStream().readNBytes(MAX_REQUEST_BODY_SIZE + 1);
            if (body.length > MAX_REQUEST_BODY_SIZE) {
                throw new InvalidIssuedKeyException(
                        "The request body exceeds " + MAX_REQUEST_BODY_SIZE + " bytes");
            }
            return new String(body, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read the request body", e);
        }
    }
}
