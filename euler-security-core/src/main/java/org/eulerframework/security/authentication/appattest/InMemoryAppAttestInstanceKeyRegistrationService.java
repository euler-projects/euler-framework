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

    private final Map<String, AppAttestInstanceKeyRegistration> keysByJwkKid = new ConcurrentHashMap<>();

    @Override
    public AppAttestInstanceKeyRegistration saveRegistration(AppAttestInstanceKeyRegistration registration) {
        Assert.notNull(registration, "registration must not be null");
        // putIfAbsent rather than put: a key ID is unique across the whole registry and is never
        // reassigned, so a collision is a conflict for the caller to settle, not an overwrite.
        // Mirrors JdbcAppAttestInstanceKeyRegistrationService, which lets the insert fail.
        if (this.keysByJwkKid.putIfAbsent(registration.jwkKid(), registration) != null) {
            throw new DuplicateInstanceKeyException(registration.jwkKid());
        }
        // Read back rather than return the argument, so both implementations answer with what
        // is registered and a caller cannot tell them apart.
        return this.keysByJwkKid.get(registration.jwkKid());
    }

    @Override
    public AppAttestInstanceKeyRegistration findByAppAttestKidAndJwkKid(String appAttestKid, String jwkKid) {
        // No row has a null key ID, so this is the same answer the JDBC implementation gives by
        // way of a WHERE clause that matches nothing - and a ConcurrentHashMap cannot even be
        // asked the question.
        if (jwkKid == null) {
            return null;
        }
        AppAttestInstanceKeyRegistration registration = this.keysByJwkKid.get(jwkKid);
        // The key ID alone is unique, but a consumer may only read the keys of the instance it has
        // authenticated - so a key ID somebody else registered is not found here.
        return registration != null && registration.appAttestKid().equals(appAttestKid) ? registration : null;
    }
}
