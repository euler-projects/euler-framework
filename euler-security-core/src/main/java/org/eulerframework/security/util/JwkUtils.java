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

package org.eulerframework.security.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.security.jwk.JwkEntry;
import org.eulerframework.security.jwk.JwkStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class JwkUtils {
    /**
     * Sentinel value when the JwkEntry list is empty.
     */
    public static final String EMPTY_FINGERPRINT = "0";

    private JwkUtils() {
        // Utility class — not instantiable.
    }

    public static String hashFingerprints(List<byte[]> fingerprintOrderedByKid) {
        if (fingerprintOrderedByKid.isEmpty()) {
            return EMPTY_FINGERPRINT;
        }
        MessageDigest md = getDigest();

        for (byte[] each : fingerprintOrderedByKid) {
            md.update(each);
            md.update((byte) '\n');
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest());
    }

    public static String hashJwkEntryFingerprints(Collection<JwkEntry> entries) {
        List<byte[]> fingerprintOrderedByKid = entries.stream()
                .sorted(Comparator.comparing(JwkEntry::kid))
                .map(JwkEntry::fingerprint)
                .toList();

        return hashFingerprints(fingerprintOrderedByKid);
    }

    public static byte[] fingerprint(JWK jwk, JwkStatus status) {
        String thumbprint;
        try {
            thumbprint = jwk.toPublicJWK().computeThumbprint().toString();
        } catch (JOSEException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        String kid = (jwk.getKeyID() == null) ? "" : jwk.getKeyID();
        String alg = (jwk.getAlgorithm() == null) ? "" : jwk.getAlgorithm().getName();
        String use = (jwk.getKeyUse() == null) ? "" : jwk.getKeyUse().getValue();
        String iat = (jwk.getIssueTime() == null) ? "" : String.valueOf(jwk.getIssueTime().getTime());
        String raw = status + "|" + kid + "|" + alg + "|" + use + "|" + iat + "|" + thumbprint;
        MessageDigest md = getDigest();
        return md.digest(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }
}
