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
package org.eulerframework.security.authentication.otp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * Internal helper that performs PKCE (RFC 7636) {@code code_verifier} -&gt;
 * {@code code_challenge} verification.
 * <p>
 * Currently only the {@code S256} method is supported; this matches the
 * framework's contract that incoming requests must always declare
 * {@code code_challenge_method=S256}.
 */
final class OtpPkceVerifier {

    static final String METHOD_S256 = "S256";

    private OtpPkceVerifier() {
    }

    /**
     * Verify that {@code codeVerifier}, when transformed using
     * {@code codeChallengeMethod}, equals {@code codeChallenge}.
     * <p>
     * When {@code codeChallenge} is {@code null} the ticket was issued in a
     * PKCE-disabled flow (see {@code euler.security.otp.pkce.enabled}); in
     * that case the verifier returns {@code true} regardless of the value of
     * {@code codeVerifier}, so PKCE is effectively skipped.
     *
     * @return {@code true} on a successful match (or when PKCE is not bound
     *         to the ticket), {@code false} otherwise
     */
    static boolean verify(String codeVerifier, String codeChallenge, String codeChallengeMethod) {
        if (codeChallenge == null) {
            // PKCE was not bound to this ticket -> nothing to verify.
            return true;
        }
        if (codeVerifier == null) {
            return false;
        }
        if (!METHOD_S256.equals(codeChallengeMethod)) {
            // The issue endpoint should already have rejected anything other
            // than S256; defensive guard for storage layer only.
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            String computed = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return Objects.equals(computed, codeChallenge);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by every JRE; this branch is unreachable in practice.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
