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
 * Strategy interface for generating challenge strings used in challenge-response
 * authentication flows.
 * <p>
 * Implementations define how the raw challenge string is produced (e.g., Base64 URL
 * encoding of random bytes, hex encoding, etc.). The storage and lifecycle management
 * of challenges is handled by {@link ChallengeService} implementations.
 *
 * @see Base64UrlChallengeGenerator
 * @see ChallengeService
 */
@FunctionalInterface
public interface ChallengeGenerator {

    /**
     * Generate a new challenge string.
     *
     * @return the generated challenge string, never {@code null} or empty
     */
    String generateChallenge();
}
