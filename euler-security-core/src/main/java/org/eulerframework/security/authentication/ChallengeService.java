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
package org.eulerframework.security.authentication;

/**
 * Service for generating and consuming one-time challenges used in authentication flows
 * that require a challenge-response mechanism (e.g., Apple App Attest, WebAuthn).
 * <p>
 * Challenges are single-use, time-limited tokens that prevent replay attacks. The server
 * generates a challenge bound to a specific client, the client uses it in verification,
 * and the server consumes (invalidates) the challenge during verification.
 */
public interface ChallengeService {

    /**
     * Generate a new one-time challenge bound to the specified client.
     *
     * @param clientId the OAuth2 client ID that this challenge is bound to
     * @return a {@link GeneratedChallenge} containing the challenge challenge and its format
     */
    GeneratedChallenge generateChallenge(String clientId);

    /**
     * Consume a previously generated challenge, marking it as used.
     * <p>
     * A challenge can only be consumed once. Subsequent calls with the same challenge
     * must return {@code false}. The clientId must match the one used when
     * the challenge was generated.
     *
     * @param challenge the challenge to consume
     * @param clientId  the OAuth2 client ID that this challenge was bound to
     * @return {@code true} if the challenge was valid, matched the client, and was
     *         successfully consumed; {@code false} if the challenge is unknown,
     *         expired, already consumed, or bound to a different client
     */
    boolean consumeChallenge(String challenge, String clientId);
}
