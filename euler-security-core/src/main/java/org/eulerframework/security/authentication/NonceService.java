/*
 * Copyright 2013-2024 the original author or authors.
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

import java.time.Duration;

/**
 * Service for tracking one-time-use nonce/jti values to prevent replay attacks.
 * <p>
 * Unlike {@link ChallengeService} which generates server-side challenges,
 * {@code NonceService} tracks client-generated values (e.g., the {@code jti}
 * claim in a PoP JWT) to ensure they are not reused.
 * <p>
 * Implementations must be thread-safe.
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html#section-12.1">
 *     Section 12.1 – Replay of Client Attestation PoP JWT</a>
 */
public interface NonceService {

    /**
     * Record a nonce and return whether it was seen for the first time.
     *
     * @param nonce the nonce/jti value to track
     * @param ttl   how long to remember this nonce (should match or exceed
     *              the PoP JWT lifetime window)
     * @return {@code true} if this is the first time seeing this nonce;
     *         {@code false} if it was already recorded (replay detected)
     */
    boolean recordIfAbsent(String nonce, Duration ttl);
}
