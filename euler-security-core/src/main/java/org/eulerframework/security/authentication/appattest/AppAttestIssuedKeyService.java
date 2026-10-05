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

/**
 * Service interface for storing and retrieving the public keys an App instance issued
 * and registered under itself as a JWT {@code iss}.
 * <p>
 * Keys are written by the App Attest key registration endpoint, which authenticates the
 * caller with an App Attest assertion before accepting anything, and are read when a
 * jwt-bearer assertion signed by one of them has to be verified. The registry is the
 * issuer side of that trust relationship and is consulted only to establish it: once a
 * key has been used to provision a {@code public_key} user identity, later logins verify
 * against the identity's own copy of the key rather than coming back here, so an entry
 * disappearing does not lock an existing account out.
 *
 * @see AppAttestIssuedKey
 */
public interface AppAttestIssuedKeyService {

    /**
     * Register a public key under its issuer, replacing any earlier registration of the same
     * key.
     * <p>
     * Implementations are idempotent on {@code (issuer, keyId)}: registering the same key
     * again neither adds an entry nor fails. Callers rely on this, because the key ID is
     * derived from the key material, so a client retrying a registration that already
     * succeeded presents the very same pair &mdash; and a client that lost the response to its
     * first attempt still has to be told the key ID, which is why a repeat answers like a
     * success rather than like a conflict.
     * <p>
     * A repeat <b>overwrites</b>, and that is safe precisely because of how the key ID is
     * derived: it is the RFC 7638 thumbprint, which covers a key's required members and
     * nothing else, so two registrations sharing a key ID are the same key by construction.
     * The only thing a later one can change is metadata the thumbprint does not cover
     * ({@code alg}, {@code use}, an {@code x5c} chain), and no signature is ever verified
     * against those &mdash; verification goes by the key's {@code kty} and material. Keeping
     * the latest therefore cannot weaken a login, while keeping the first would leave the
     * registry describing a key differently from how its own issuer now presents it.
     * <p>
     * Note what this does make the registry: a row's metadata is rewritable by whoever can
     * authenticate as its issuer. That is inert today because nothing reads it. A consumer
     * added later &mdash; anything trusting {@code x5c} for a certificate path, say &mdash;
     * would be inheriting caller-controlled input and must validate it as such.
     *
     * @param key the key to register; never {@code null}
     * @return the key as registered after this call, read back from the store rather than
     *         echoed from the argument, so that a caller reports what was persisted; never
     *         {@code null}
     */
    AppAttestIssuedKey saveKey(AppAttestIssuedKey key);

    /**
     * Retrieve a registered key by its issuer and key ID.
     *
     * @param issuer the JWT {@code iss} the key was registered under
     * @param keyId  the JWT {@code kid}
     * @return the key, or {@code null} if the issuer has no such key
     */
    AppAttestIssuedKey findByIssuerAndKeyId(String issuer, String keyId);
}
