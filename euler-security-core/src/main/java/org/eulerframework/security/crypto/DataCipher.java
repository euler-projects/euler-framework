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

/**
 * Contract of a string-level cipher.
 *
 * <p>An implementation transforms a plaintext {@link String} into an opaque
 * ciphertext {@link String} (typically a self-describing {@link
 * EncryptedEnvelopeCodec envelope}) and back. Implementations are expected to
 * stamp enough algorithm/key identification into their ciphertext so that a
 * {@link DelegatingDataCipher} can pick the right implementation for decrypt.
 *
 * <p>A {@code null} input is passed through unchanged in both directions.
 */
public interface DataCipher {

    /** Encrypt the plaintext; {@code null} is returned unchanged. */
    String encrypt(String plaintext);

    /** Decrypt the ciphertext; {@code null} is returned unchanged. */
    String decrypt(String ciphertext);
}
