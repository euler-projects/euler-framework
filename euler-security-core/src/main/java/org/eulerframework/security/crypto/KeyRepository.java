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

import java.util.Set;

/**
 * Lookup API for data-encryption keys, organized by algorithm identifier
 * and key identifier ({@code kid}).
 *
 * <p>Each algorithm has exactly one <em>primary</em> key used for writes;
 * any non-primary keys are kept around only to allow historical ciphertexts
 * to be decrypted. Algorithms that do not use keys at all (e.g. {@code
 * noop}) are not registered here — they are wired directly into
 * {@link DelegatingDataCipher}.
 */
public interface KeyRepository {

    /**
     * Returns the primary key for {@code alg} — the one that new ciphertexts
     * should be encrypted under. Throws {@link IllegalStateException} when
     * {@code alg} is not registered.
     */
    KeyEntry getPrimaryKey(String alg);

    /**
     * Returns the key identified by {@code (alg, kid)}. Throws
     * {@link IllegalStateException} when no such key is registered — callers
     * should treat this as a data-integrity error.
     */
    KeyEntry getKey(String alg, String kid);

    /** {@code true} if at least one key is registered under {@code alg}. */
    boolean supports(String alg);

    /** All algorithm identifiers known to this repository. */
    Set<String> algorithms();
}
