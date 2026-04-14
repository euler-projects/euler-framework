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
package org.eulerframework.security.authentication;

import org.springframework.util.Assert;

/**
 * Represents a generated challenge with its unique ID, value, and format descriptor.
 * <p>
 * The {@code id} field is a unique identifier for the challenge, allowing clients to
 * reference it later when consuming the challenge. The {@code format} field describes
 * the encoding/format of the challenge value.
 *
 * @param id        the unique identifier for this challenge
 * @param challenge the challenge string
 * @param format    optional, the format descriptor of the challenge
 * @see ChallengeService
 */
public record GeneratedChallenge(String id, String challenge, String format) {

    /**
     * Creates a {@code GeneratedChallenge} with the specified id and challenge string
     * without format info
     *
     * @param id        the unique identifier for this challenge
     * @param challenge the challenge string
     */
    public GeneratedChallenge(String id, String challenge) {
        this(id, challenge, null);
    }

    public GeneratedChallenge {
        Assert.hasText(id, "id must not be empty");
        Assert.hasText(challenge, "challenge must not be empty");
    }
}
