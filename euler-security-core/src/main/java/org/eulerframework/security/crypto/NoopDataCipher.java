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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Null {@link DataCipher} that wraps the plaintext unchanged into a single
 * envelope body segment. Intended for development, testing and
 * historical-data compatibility; has no KEY and therefore its envelope
 * header omits the {@code kid} field. It does not depend on any
 * {@link KeyRepository}.
 *
 * <p>Using this cipher for production data is unsafe — a {@code WARN} is
 * emitted on construction so that the choice is never accidental.
 */
public final class NoopDataCipher implements DataCipher {

    /** Stable algorithm identifier used in envelope headers and registry keys. */
    public static final String ALGORITHM = "noop";

    private static final Logger log = LoggerFactory.getLogger(NoopDataCipher.class);

    public NoopDataCipher() {
        log.warn("NoopDataCipher is active — data-encryption algorithm '{}' stores values unencrypted. " +
                "Use only for development, testing or historical-data compatibility.", ALGORITHM);
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        return EncryptedEnvelopeCodec.encode(ALGORITHM, null, EncryptedEnvelopeCodec.toUtf8(plaintext));
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        EncryptedEnvelopeCodec.Envelope envelope = EncryptedEnvelopeCodec.decode(ciphertext);
        if (!ALGORITHM.equals(envelope.algorithmId())) {
            throw new IllegalStateException(
                    "NoopDataCipher received envelope with alg '" + envelope.algorithmId() + "'");
        }
        List<byte[]> bodyParts = envelope.bodyParts();
        if (bodyParts.size() != 1) {
            throw new IllegalStateException(
                    "NoopDataCipher expects 1 body part, got " + bodyParts.size());
        }
        return EncryptedEnvelopeCodec.fromUtf8(bodyParts.getFirst());
    }
}
