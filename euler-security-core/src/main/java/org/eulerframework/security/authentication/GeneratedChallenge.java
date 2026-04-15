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
 * Represents a generated challenge with its value.
 * <p>
 * The challenge value is returned to the client and must be returned by the client
 * during verification. The server stores the challenge and consumes (invalidates) it
 * when the client presents it for verification.
 *
 * @param challenge the challenge string
 * @see ChallengeService
 */
public record GeneratedChallenge(String challenge) {

    public GeneratedChallenge {
        Assert.hasText(challenge, "challenge must not be empty");
    }
}
