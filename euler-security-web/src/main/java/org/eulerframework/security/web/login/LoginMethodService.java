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
 * Serves the login methods a deployment offers, projected for
 * publication.
 *
 * <p>It reads the registrations from the
 * {@link RegisteredLoginMethodRepository} and hands each one to the
 * {@link LoginMethodHandler} serving its type, so a caller receives
 * ready-to-render {@link LoginMethod}s without knowing which types exist
 * or how any of them is configured.
 *
 * <p>Whether a method is available at all is settled here rather than by
 * the caller: a registration whose handler cannot currently serve it
 * (say it points at an undefined OAuth2 client registration) is left
 * out, and callers never inspect enablement themselves.
 *
 * <p>The bundled login page is one consumer; an SPA or a native client
 * reading the same list is another.
 */
public interface LoginMethodService {

    /**
     * Returns every login method currently on offer, in the order they
     * are to be presented in. Never {@code null}; empty only when the
     * runtime configuration produces nothing servable.
     */
    List<LoginMethod> listAll();
}
