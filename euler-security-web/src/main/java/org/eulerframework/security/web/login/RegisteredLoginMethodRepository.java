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

import java.util.List;

/**
 * A repository for the {@link RegisteredLoginMethod}s a deployment
 * offers.
 *
 * <p>This is the single registry every login method reaches the runtime
 * through, whichever type it is and whoever declared it: the login page
 * renders {@link #findAll()}, and dispatch resolves the method a
 * submission selects out of it. Uniqueness of the
 * {@link RegisteredLoginMethod#getName() name} clients address a method
 * by is therefore a repository-wide invariant.
 *
 * <p>The default implementation is
 * {@link DefaultRegisteredLoginMethodRepository}, populated from
 * configuration at startup. Publishing another bean of this type
 * replaces it, which is the seam for serving login methods out of a
 * database or an administration console instead.
 */
public interface RegisteredLoginMethodRepository {

    /**
     * Saves a registration.
     *
     * @param loginMethod the registration to save; must not be null
     */
    void save(RegisteredLoginMethod loginMethod);

    /**
     * Returns the registration stored under {@code id}, or {@code null}
     * if there is none. Note that clients address methods by name, not
     * by id, so this is a maintenance rather than a runtime lookup.
     *
     * @param id the id to look up; must not be empty
     */
    RegisteredLoginMethod findById(String id);

    /**
     * Returns every registration held, in the order they are to be
     * offered in; empty when none is held.
     *
     * <p>Order is significant: it drives the order the login page
     * renders the methods in, and which one it expands when no method
     * declares itself primary. Implementations must return a stable
     * order rather than whatever a hash yields.
     */
    List<RegisteredLoginMethod> findAll();
}
