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

package org.eulerframework.security.authentication.appattest;

import org.eulerframework.security.util.JwkUtils;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link AppAttestInstanceKeyRegistrationService}, backed by a
 * {@link ConcurrentHashMap}. Registered keys are lost on restart.
 * <p>
 * A test double and a development convenience only. Auto-configuration deliberately does
 * <b>not</b> fall back to this when no {@link JdbcAppAttestInstanceKeyRegistrationService} can be
 * built, unlike {@code RegisteredAppRepository}, whose in-memory form is safe because it is
 * repopulated from configuration properties on every start. What this holds is state accumulated
 * at runtime, and losing it degrades silently rather than visibly: every App instance has to
 * re-attest and re-register its keys, and any consumer that had already recorded a key elsewhere
 * keeps working while a first use of one that was only registered here stops resolving. Failing
 * to start is the better outcome, so an application that genuinely has no {@code JdbcOperations}
 * must declare its own {@link AppAttestInstanceKeyRegistrationService} bean and own that
 * consequence.
 *
 * @see AppAttestInstanceKeyRegistrationService
 * @see JdbcAppAttestInstanceKeyRegistrationService
 */
public class InMemoryAppAttestInstanceKeyRegistrationService implements AppAttestInstanceKeyRegistrationService {

    private final Map<String, AppAttestInstanceKeyRegistration> keys = new ConcurrentHashMap<>();

    @Override
    public AppAttestInstanceKeyRegistration saveRegistration(AppAttestInstanceKeyRegistration registration) {
        Assert.notNull(registration, "registration must not be null");
        // Same rule the JDBC implementation applies, for the same reason: a consumer answers a
        // lookup by kid with the jwk filed under it, so the two have to describe one key.
        JwkUtils.requireThumbprintKeyId(registration.jwkKid(), registration.jwk());
        String indexKey = indexKey(registration.appAttestKid(), registration.jwkKid());
        // put rather than putIfAbsent: the key ID is the thumbprint of the key material, so a
        // repeated registration is by definition the same key, and the latest description of
        // it is the one worth keeping. Mirrors JdbcAppAttestInstanceKeyRegistrationService,
        // which updates the row an insert collided with.
        this.keys.put(indexKey, registration);
        // Read back rather than return the argument, so both implementations answer with what
        // is registered and a caller cannot tell them apart.
        return this.keys.get(indexKey);
    }

    @Override
    public AppAttestInstanceKeyRegistration findByAppAttestKidAndJwkKid(String appAttestKid, String jwkKid) {
        return this.keys.get(indexKey(appAttestKid, jwkKid));
    }

    /**
     * Flatten the composite key into one map key. The separator cannot occur in either part:
     * an App Attest {@code kid} and a Base64URL thumbprint are both drawn from alphabets that
     * exclude it.
     */
    private static String indexKey(String appAttestKid, String jwkKid) {
        return appAttestKid + '\n' + jwkKid;
    }
}
