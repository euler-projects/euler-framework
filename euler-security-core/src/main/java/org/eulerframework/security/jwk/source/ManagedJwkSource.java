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
package org.eulerframework.security.jwk.source;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.Nonnull;
import org.eulerframework.security.jwk.JwkEntry;
import org.eulerframework.security.jwk.JwkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public final class ManagedJwkSource implements JWKSource<SecurityContext> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ManagedJwkSource.class);

    private final JwkRepository repository;
    private final ReentrantLock reloadLock = new ReentrantLock();

    private volatile LiveState state;

    public ManagedJwkSource(JwkRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
        if (this.state == null) {
            this.reload();
        }
        return jwkSelector.select(this.state.jwks());
    }

    public void reload() {
        this.reloadLock.lock();
        try {
            String nextFingerprint = this.repository.fingerprint();
            LiveState current = this.state;
            if (current != null && current.fingerprint().equals(nextFingerprint)) {
                LOGGER.debug("Jwk source reload: fingerprint unchanged ({}), no-op", nextFingerprint);
                return;
            }

            List<JWK> jwks = this.repository.load().stream()
                    .filter(JwkEntry::isSignatureKey)
                    .filter(entry -> !entry.isExpired()) // 去掉过期的
                    // 确保有私钥的在前, 都有私钥的签发时间晚的在前
                    .sorted(Comparator
                            .comparing(JwkEntry::hasPrivateKey) // 有私钥的在后
                            .thenComparing(JwkEntry::iat) // 签发时间晚的在后
                            .reversed() // 倒转, 变成有私钥的在前, 签发时间晚的在前
                            .thenComparing(JwkEntry::isPending) // pending的在后
                    )
                    .map(JwkEntry::jwk)
                    .toList();

            JWKSet jwkSet = new JWKSet(jwks);

            LiveState next = new LiveState(jwkSet, nextFingerprint);

            this.state = next;

            if (LOGGER.isDebugEnabled()) {
                List<String> kids = jwks.stream().map(JWK::getKeyID).toList();
                LOGGER.debug("Jwk source reload: fingerprint: {}, sorted kids: {}", next.fingerprint, kids);
            }
        } finally {
            this.reloadLock.unlock();
        }
    }


    record LiveState(@Nonnull JWKSet jwks, @Nonnull String fingerprint) {
    }
}
