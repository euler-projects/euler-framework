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
 * Registry of the public keys App instances register under themselves.
 * <p>
 * Written by the App Attest key registration endpoint, which authenticates the caller with an
 * App Attest assertion before accepting anything, so an entry says "this App instance vouches
 * for this key". What a consumer then does with the key is its own business and is not
 * described here: this is the App Attest domain's registry, and it holds no OAuth2, no token
 * and no user vocabulary.
 * <p>
 * A consumer that has independently established which App instance it is talking to looks the
 * key up by that instance's App Attest KEY and the {@code kid} naming the key. It is the
 * consumer's job to have established the instance &mdash; the registry only records what was
 * registered, and never resolves an instance from a key on its own.
 *
 * @see AppAttestInstanceKeyRegistration
 */
public interface AppAttestInstanceKeyRegistrationService {

    /**
     * Register a public key under the App instance that submitted it, replacing any earlier
     * registration of the same key by the same instance.
     * <p>
     * Implementations are idempotent on {@code (appAttestKid, jwkKid)}: registering the same key
     * again neither adds an entry nor fails. Callers rely on this, because the key ID is derived
     * from the key material, so a client retrying a registration that already succeeded presents
     * the very same pair &mdash; and a client that lost the response to its first attempt still
     * has to be told the key ID, which is why a repeat answers like a success rather than like a
     * conflict.
     * <p>
     * A repeat <b>overwrites</b>, and that is safe precisely because of how the key ID is derived:
     * it is the RFC 7638 thumbprint, which covers a key's required members and nothing else, so
     * two registrations sharing a key ID are the same key by construction. The only thing a later
     * one can change is metadata the thumbprint does not cover ({@code alg}, {@code use}, an
     * {@code x5c} chain), and no signature is ever verified against those &mdash; verification goes
     * by the key's {@code kty} and material. Keeping the latest therefore cannot weaken a consumer,
     * while keeping the first would leave the registry describing a key differently from how its
     * instance now presents it.
     * <p>
     * Note what this does make the registry: a row's metadata is rewritable by whoever can
     * authenticate as its instance. That is inert today because nothing reads it. A consumer added
     * later &mdash; anything trusting {@code x5c} for a certificate path, say &mdash; would be
     * inheriting caller-controlled input and must validate it as such.
     *
     * @param registration the registration to save; never {@code null}
     * @return the registration as stored after this call, read back from the store rather than
     *         echoed from the argument, so that a caller reports what was persisted; never
     *         {@code null}
     */
    AppAttestInstanceKeyRegistration saveRegistration(AppAttestInstanceKeyRegistration registration);

    /**
     * Retrieve the registration of a key an App instance registered.
     *
     * @param appAttestKid the App Attest KEY identifying the instance
     * @param jwkKid       the {@code kid} of the key wanted
     * @return the registration, or {@code null} if that instance has registered no such key
     */
    AppAttestInstanceKeyRegistration findByAppAttestKidAndJwkKid(String appAttestKid, String jwkKid);
}
