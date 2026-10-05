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

package org.eulerframework.security.authentication.appattest;

import org.springframework.util.Assert;

/**
 * A public key an authenticated App instance issued and registered under itself as a
 * JWT {@code iss}, so that it can later sign jwt-bearer assertions the authorization
 * server accepts.
 * <p>
 * This is the <b>issuer-side</b> half of the jwt-bearer trust relationship: it answers
 * "which key does this issuer's assertion signature verify against", and nothing else.
 * It carries no user semantics &mdash; no account is created or referenced by
 * registering one. The user-side counterpart is the {@code public_key} user identity,
 * which a first jwt-bearer login provisions separately; the two are deliberately not
 * joined, so that revoking or re-registering the App Attest KEY an issuer was
 * authenticated with does not disturb the identities already bound to accounts.
 *
 * @param issuer the JWT {@code iss} the key was registered under; for an App Attest App
 *               instance this is its OAuth2 {@code client_id}
 * @param keyId  the JWT {@code kid} selecting this key among the issuer's keys; the
 *               RFC 7638 JWK Thumbprint of {@code jwk}, derived by the server rather
 *               than supplied by the client
 * @param jwk    the public JWK (RFC 7517) as JSON, carrying no private members
 * @see AppAttestIssuedKeyService
 */
public record AppAttestIssuedKey(String issuer, String keyId, String jwk) {

    public AppAttestIssuedKey {
        Assert.hasText(issuer, "issuer must not be empty");
        Assert.hasText(keyId, "keyId must not be empty");
        Assert.hasText(jwk, "jwk must not be empty");
    }
}
