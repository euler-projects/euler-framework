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
 * Thrown when a public key is registered under a key ID the registry already holds.
 * <p>
 * A key ID is chosen by the registering instance, is unique across the whole registry and is
 * never reassigned, so a collision is not something the store can settle by overwriting: the row
 * already there may belong to another instance, and even when it does not, replacing the key an
 * account may already be bound to would strand that account with nothing left to detect it by.
 * Registration is therefore not idempotent on the key ID, and the caller picks another one.
 * <p>
 * What a caller loses by this is nothing it did not already have: it chose the key ID, so it does
 * not need the response to learn it, and a registration whose response was lost is simply used
 * anyway &mdash; the key is stored. A caller that cannot tell whether its first attempt landed
 * finds out at the first login that names the key.
 * <p>
 * This is a conflict with what the registry already holds rather than a fault of the request or of
 * the caller's credentials, so an endpoint reports it as such and neither as a {@code 400} nor as
 * a {@code 401}. Which key IDs are taken is thereby discoverable by anyone who can authenticate
 * as an App instance, exactly as a taken username is; a key ID that is a fresh UUID is not
 * guessable, and one that is not carries no secret anyway.
 *
 * @see AppAttestInstanceKeyRegistrationService#saveRegistration
 */
public class DuplicateInstanceKeyException extends RuntimeException {

    private final String jwkKid;

    public DuplicateInstanceKeyException(String jwkKid) {
        super("The key ID '" + jwkKid + "' is already registered");
        this.jwkKid = jwkKid;
    }

    /**
     * The key ID that was already taken, as the caller supplied it.
     */
    public String getJwkKid() {
        return this.jwkKid;
    }
}
