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

package org.eulerframework.security.oauth2.server.authorization.util;

import org.eulerframework.security.oauth2.server.authorization.jwk.JwkEntry;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public final class JwkUtils {
    /**
     * Sentinel value when the JwkEntry list is empty.
     */
    public static final String EMPTY_FINGERPRINT = "0";

    private JwkUtils() {
        // Utility class — not instantiable.
    }


    public static String fingerprint(List<JwkEntry> ordered) {
        if (ordered.isEmpty()) {
            return EMPTY_FINGERPRINT;
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable in the JRE", ex);
        }
        boolean first = true;
        for (JwkEntry entry : ordered) {
            if (!first) {
                md.update((byte) '|');
            }
            md.update(entry.contentFingerprint().getBytes(StandardCharsets.UTF_8));
            first = false;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest());
    }
}
