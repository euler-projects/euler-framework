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
package org.eulerframework.security.crypto;

import org.springframework.util.Assert;

/**
 * A single key registered in a {@link KeyRepository}.
 *
 * <p>Used only by algorithms that require keyed cryptography (e.g.
 * AES-256-GCM). Algorithms without keys (such as {@code noop}) do not go
 * through {@link KeyRepository} at all.
 *
 * @param alg      algorithm identifier this key belongs to, e.g. {@code
 *                 "AES-256-GCM"}; never blank
 * @param kid      key identifier, unique within {@code alg}; never blank
 * @param material raw key material the algorithm expects (e.g. 32 bytes for
 *                 AES-256); never {@code null}, never empty
 */
public record KeyEntry(String alg, String kid, byte[] material) {

    public KeyEntry {
        Assert.hasText(alg, "alg must not be blank");
        Assert.hasText(kid, "kid must not be blank");
        Assert.notNull(material, "material must not be null");
        if (material.length == 0) {
            throw new IllegalArgumentException("material must not be empty");
        }
    }
}
