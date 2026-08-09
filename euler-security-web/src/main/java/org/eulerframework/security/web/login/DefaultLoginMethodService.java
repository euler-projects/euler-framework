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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;

/**
 * {@link LoginMethodService} that reads the registrations held by a
 * {@link RegisteredLoginMethodRepository} and delegates each one to the
 * {@link LoginMethodHandler} matching its {@code type}.
 *
 * <p>Type-agnostic by construction: it indexes the handlers it is given
 * by {@link LoginMethodHandler#type() type} and knows nothing about any
 * particular one, so a type the framework never shipped is served as
 * soon as its handler and its registrations are present. A registration
 * whose type no handler serves is skipped with a WARN log rather than
 * failing the application.
 *
 * <p>When the repository holds nothing, a default {@code password}
 * registration is synthesized so the login page always renders at least
 * the password form.
 *
 * <p>Each registration is exposed under its
 * {@link RegisteredLoginMethod#getName() name}; its id is a storage
 * concern (it may be an opaque key) and is never published. Names must
 * be unique, and a collision that reached this point is rejected rather
 * than silently served.
 *
 * <p>Beyond the {@link LoginMethodService} contract this also exposes
 * {@link #resolve(String)}, which the routing filter needs to find the
 * registration and handler a submitted method name selects.
 */
public class DefaultLoginMethodService implements LoginMethodService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, LoginMethodHandler> handlersByType;
    private final RegisteredLoginMethodRepository registeredLoginMethodRepository;

    public DefaultLoginMethodService(
            List<LoginMethodHandler> handlers,
            RegisteredLoginMethodRepository registeredLoginMethodRepository) {
        Assert.notNull(handlers, "handlers is required");
        Assert.notNull(registeredLoginMethodRepository, "registeredLoginMethodRepository is required");
        Map<String, LoginMethodHandler> index = new HashMap<>(handlers.size());
        for (LoginMethodHandler handler : handlers) {
            String typeName = handler.type();
            Assert.hasText(typeName, () -> "LoginMethodHandler#type() returned blank on "
                    + handler.getClass().getName());
            LoginMethodHandler previous = index.put(typeName, handler);
            if (previous != null) {
                throw new IllegalStateException("Duplicate LoginMethodHandler for type='"
                        + typeName + "': " + previous.getClass().getName() + " and "
                        + handler.getClass().getName());
            }
        }
        this.handlersByType = Collections.unmodifiableMap(index);
        this.registeredLoginMethodRepository = registeredLoginMethodRepository;
    }

    @Override
    public List<LoginMethod> listAll() {
        Map<String, ResolvedLoginMethod> byName = indexByName();
        List<LoginMethod> methods = new ArrayList<>(byName.size());
        for (ResolvedLoginMethod resolved : byName.values()) {
            LoginMethod described = resolved.handler().describe(resolved.method());
            if (described == null) {
                continue;
            }
            methods.add(described);
        }
        return Collections.unmodifiableList(methods);
    }

    /**
     * Resolves the registration and handler the given method name
     * selects, for the routing filter to dispatch with. Returns
     * {@code null} if no method is offered under that name.
     */
    public ResolvedLoginMethod resolve(String name) {
        return indexByName().get(name);
    }

    /**
     * Indexes every servable registration by its name.
     *
     * @throws IllegalStateException if two registrations share a name,
     *                               leaving the name ambiguous to
     *                               dispatch
     */
    private Map<String, ResolvedLoginMethod> indexByName() {
        Collection<RegisteredLoginMethod> loginMethods = resolveLoginMethods();
        Map<String, ResolvedLoginMethod> index = new LinkedHashMap<>(loginMethods.size());
        for (RegisteredLoginMethod method : loginMethods) {
            if (method == null) {
                continue;
            }
            LoginMethodHandler handler = this.handlersByType.get(method.getType());
            if (handler == null) {
                this.logger.warn("Login method '{}' declares type='{}' but no LoginMethodHandler "
                        + "is registered for that type; skipping. Registered types: {}",
                        method.getId(), method.getType(), this.handlersByType.keySet());
                continue;
            }
            ResolvedLoginMethod previous = index.put(method.getName(),
                    new ResolvedLoginMethod(method, handler));
            if (previous != null) {
                throw new IllegalStateException("Duplicate login method name '" + method.getName()
                        + "' (registrations '" + previous.method().getId() + "' and '"
                        + method.getId() + "'): declare a distinct method-name on one of them.");
            }
        }
        return index;
    }

    private Collection<RegisteredLoginMethod> resolveLoginMethods() {
        Collection<RegisteredLoginMethod> loginMethods = this.registeredLoginMethodRepository.findAll();
        if (loginMethods == null || loginMethods.isEmpty()) {
            // Synthesize a default password method so the login page
            // always renders at least the password form.
            return List.of(new RegisteredPasswordLoginMethod(
                    RegisteredPasswordLoginMethod.TYPE, RegisteredPasswordLoginMethod.TYPE, true));
        }
        return loginMethods;
    }

    /**
     * A registration paired with the handler serving it, as dispatch
     * needs both: the handler decides the action, the registration tells
     * it which method it is acting for.
     */
    public record ResolvedLoginMethod(RegisteredLoginMethod method, LoginMethodHandler handler) {
    }
}
