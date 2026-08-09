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

package org.eulerframework.security.web.login;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory {@link RegisteredLoginMethodRepository}, holding the login
 * methods a deployment offers.
 *
 * <p>Registrations are returned in the order they were saved in. That
 * order is user-visible: the login page renders the methods in it, and
 * promotes the first one when none declares itself primary. A hash-based
 * store would let the expanded method drift between restarts.
 *
 * <p>Both identifiers a registration carries are unique repository-wide:
 * the {@code id} it is stored under, and the {@code name} clients address
 * it by. Since ids are only unique within their own
 * {@code euler.security.login-method.<method-type>} node, two types
 * declaring the same key collide here &mdash; deliberately, and at the
 * moment the registry is populated rather than on the first login page
 * render.
 */
public final class DefaultRegisteredLoginMethodRepository implements RegisteredLoginMethodRepository {

    private final Map<String, RegisteredLoginMethod> idRegistrationMap =
            Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Creates an empty repository, to be populated through
     * {@link #save(RegisteredLoginMethod)}.
     */
    public DefaultRegisteredLoginMethodRepository() {
    }

    /**
     * @param registrations the registrations to hold, in the order they
     *                      are to be offered in
     */
    public DefaultRegisteredLoginMethodRepository(RegisteredLoginMethod... registrations) {
        this(Arrays.asList(registrations));
    }

    /**
     * @param registrations the registrations to hold, in the order they
     *                      are to be offered in
     * @throws IllegalArgumentException if two registrations share an id
     *                                  or a name
     */
    public DefaultRegisteredLoginMethodRepository(List<RegisteredLoginMethod> registrations) {
        Assert.notNull(registrations, "registrations must not be null");
        registrations.forEach(this::save);
    }

    /**
     * Adds a registration, keeping it behind the ones already saved.
     *
     * <p>Registrations are added, never replaced: a repository serving a
     * deployment's declared login methods is populated once, and a
     * repeated id means two declarations claim the same slot.
     *
     * @throws IllegalArgumentException if the id or the name is already
     *                                  taken by another registration
     */
    @Override
    public void save(RegisteredLoginMethod loginMethod) {
        Assert.notNull(loginMethod, "loginMethod must not be null");
        synchronized (this.idRegistrationMap) {
            assertUniqueIdentifiers(loginMethod, this.idRegistrationMap);
            this.idRegistrationMap.put(loginMethod.getId(), loginMethod);
        }
    }

    /**
     * @return the registration stored under {@code id}, or {@code null}
     *         if there is none
     */
    @Override
    public RegisteredLoginMethod findById(String id) {
        Assert.hasText(id, "id must not be empty");
        return this.idRegistrationMap.get(id);
    }

    /**
     * @return every registration held, in the order it was saved in;
     *         empty when none was
     */
    @Override
    public List<RegisteredLoginMethod> findAll() {
        synchronized (this.idRegistrationMap) {
            return List.copyOf(this.idRegistrationMap.values());
        }
    }

    private static void assertUniqueIdentifiers(RegisteredLoginMethod loginMethod,
                                                Map<String, RegisteredLoginMethod> registrations) {
        registrations.values().forEach((registration) -> {
            if (registration.getId().equals(loginMethod.getId())) {
                throw new IllegalArgumentException("Registered login method must be unique. "
                        + "Found duplicate identifier: " + loginMethod.getId());
            }
            if (registration.getName().equals(loginMethod.getName())) {
                throw new IllegalArgumentException("Registered login method must be unique. "
                        + "Found duplicate name: " + loginMethod.getName()
                        + " (registrations '" + registration.getId() + "' and '"
                        + loginMethod.getId() + "')");
            }
        });
    }
}
