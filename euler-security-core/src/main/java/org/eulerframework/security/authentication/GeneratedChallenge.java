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

import org.springframework.util.Assert;

/**
 * Represents a generated challenge with its challenge and format descriptor.
 * <p>
 * The {@code format} field describes the encoding/format of the challenge,
 * allowing clients to interpret the challenge correctly. Different verification
 * mechanisms may produce challenges in different formats.
 *
 * @param challenge the challenge string
 * @param format    optional, the format descriptor of the challenge
 * @see ChallengeService
 */
public record GeneratedChallenge(String challenge, String format) {

    /**
     * Creates a {@code GeneratedChallenge} with the specified challenge string
     * without format info
     *
     * @param challenge the challenge string
     */
    public GeneratedChallenge(String challenge) {
        this(challenge, null);
    }

    public GeneratedChallenge {
        Assert.hasText(challenge, "challenge must not be empty");
    }
}
