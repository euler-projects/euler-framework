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

import com.nimbusds.jose.Algorithm;
import org.eulerframework.security.util.JwkUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class InMemoryJwkRepository implements org.eulerframework.security.jwk.JwkRepository {

    private final Object writeLock = new Object();
    private final ConcurrentHashMap<String, JwkEntry> store = new ConcurrentHashMap<>();

    public InMemoryJwkRepository(Collection<JwkEntry> initialEntries) {
        if (!CollectionUtils.isEmpty(initialEntries)) {
            for (JwkEntry jwkEntry : initialEntries) {
                this.save(jwkEntry);
            }
        }
    }

    @Override
    public List<JwkEntry> load() {
        return List.copyOf(this.store.values());
    }

    @Override
    public void save(JwkEntry entry) {
        Assert.notNull(entry, "JWK entry cannot be null");

        if (JwkStatus.PENDING.equals(entry.status()) || JwkStatus.ACTIVE.equals(entry.status())) {
            Assert.isTrue(entry.hasPrivateKey(), "PENDING 和 ACTIVE 状态的 key 必需包含私钥");
        }

        // store 作为单一真实数据源, 通过锁保证"同算法 ACTIVE 唯一"的检查与写入原子完成,
        // 避免与冗余索引并发下的读-改-写竞争以及状态迁移后遗留脏条目.
        synchronized (this.writeLock) {
            if (JwkStatus.ACTIVE.equals(entry.status())) {
                Algorithm alg = entry.jwk().getAlgorithm();
                for (JwkEntry existing : this.store.values()) {
                    if (!existing.kid().equals(entry.kid())
                            && JwkStatus.ACTIVE.equals(existing.status())
                            && alg.equals(existing.jwk().getAlgorithm())) {
                        throw new IllegalArgumentException("At most one ACTIVE key per algorithm is allowed, existing kid: "
                                + existing.kid());
                    }
                }
            }
            this.store.put(entry.kid(), entry);
        }
    }

    @Override
    public String fingerprint() {
        return JwkUtils.hashJwkEntryFingerprints(store.values());
    }
}
