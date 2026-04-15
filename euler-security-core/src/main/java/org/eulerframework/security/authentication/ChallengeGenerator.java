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
 * Strategy interface for generating raw challenge data used in challenge-response
 * authentication flows.
 * <p>
 * Implementations are responsible only for producing raw binary challenge data.
 * Encoding (e.g., Base64 URL) and lifecycle management are handled by
 * {@link ChallengeService} implementations.
 *
 * @see SecureRandomChallengeGenerator
 * @see ChallengeService
 */
@FunctionalInterface
public interface ChallengeGenerator {

    /**
     * Generate new raw challenge bytes.
     *
     * @return the generated challenge bytes, never {@code null} or empty
     */
    byte[] generateChallenge();
}
