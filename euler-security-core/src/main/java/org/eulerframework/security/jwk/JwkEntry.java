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
package org.eulerframework.security.jwk;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import jakarta.annotation.Nonnull;
import org.eulerframework.security.util.JwkUtils;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Date;

public record JwkEntry(@Nonnull JWK jwk, @Nonnull JwkStatus status,
                       @Nonnull byte[] fingerprint) {
    public JwkEntry(@Nonnull JWK jwk, @Nonnull JwkStatus status) {
        this(jwk, status, JwkUtils.fingerprint(jwk, status));
    }

    public JwkEntry {
        Assert.notNull(jwk, "jwk is required");
        Assert.notNull(status, "status is required");
        Assert.notNull(fingerprint, "fingerprint is required");
        Assert.notNull(jwk.getKeyID(), "kid is required");
        Assert.notNull(jwk.getAlgorithm(), "alg is required");
        Assert.notNull(jwk.getIssueTime(), "iat is required");
        if (JwkStatus.PENDING.equals(status) || JwkStatus.ACTIVE.equals(status)) {
            Assert.isTrue(jwk.isPrivate(), () -> "privateKey is required for " + status + " key");
        }
    }

    @Nonnull
    public String kid() {
        return this.jwk.getKeyID();
    }

    @Nonnull
    public Instant iat() {
        return this.jwk.getIssueTime().toInstant();
    }

    public boolean hasPrivateKey() {
        return this.jwk.isPrivate();
    }

    public boolean isSignatureKey() {
        return KeyUse.SIGNATURE.equals(this.jwk.getKeyUse());
    }

    public boolean isPending() {
        return JwkStatus.PENDING.equals(this.status) ||
                jwk.getNotBeforeTime() != null && jwk.getNotBeforeTime().after(new Date());
    }

    public boolean isExpired() {
        return JwkStatus.RETIRED.equals(this.status) ||
                jwk.getExpirationTime() != null && jwk.getExpirationTime().before(new Date());
    }

    @Nonnull
    @Override
    public String toString() {
        return "JwkEntry{kid=" + this.kid()
                + ", alg=" + this.jwk.getAlgorithm()
                + ", status=" + this.status
                + ", hasPrivate=" + this.hasPrivateKey() + '}';
    }
}
