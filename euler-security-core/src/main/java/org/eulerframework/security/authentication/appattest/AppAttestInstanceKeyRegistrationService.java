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
     * Register a public key under the App instance that submitted it.
     * <p>
     * Insert-only. A key ID is unique across the whole registry and is never reassigned, so a
     * collision is a conflict for the caller to settle and not something to overwrite: the row
     * already there may belong to another instance, and even when it does not, replacing the key
     * an account may already be bound to would strand that account with nothing left to detect it
     * by. A registered row is therefore immutable, which also means no part of it is rewritable by
     * whoever can authenticate as its instance &mdash; a consumer added later that reads more of
     * the JWK than its {@code kty} and key material inherits nothing caller-controlled.
     * <p>
     * This is a {@code POST} and behaves like one: a repeat of a registration that already
     * succeeded is refused rather than answered as if it had. The caller loses nothing by it,
     * because the key ID is one it chose &mdash; it does not need the response to learn it, and a
     * registration whose response was lost is stored all the same.
     *
     * @param registration the registration to save; never {@code null}
     * @return the registration as stored, read back from the store rather than echoed from the
     *         argument, so that a caller reports what was persisted; never {@code null}
     * @throws DuplicateInstanceKeyException if the registry already holds {@code jwkKid}
     */
    AppAttestInstanceKeyRegistration saveRegistration(AppAttestInstanceKeyRegistration registration);

    /**
     * Retrieve the registration of a key an App instance registered.
     * <p>
     * Both halves are required even though {@code jwkKid} alone is unique: a consumer may only
     * read the keys registered by the instance it has itself authenticated, and naming that
     * instance in the query is what enforces it. A key ID registered by someone else is not found
     * here, so one instance cannot answer for another's key however it learned the identifier.
     *
     * @param appAttestKid the App Attest KEY identifying the instance
     * @param jwkKid       the {@code kid} of the key wanted
     * @return the registration, or {@code null} if that instance has registered no such key
     */
    AppAttestInstanceKeyRegistration findByAppAttestKidAndJwkKid(String appAttestKid, String jwkKid);
}
