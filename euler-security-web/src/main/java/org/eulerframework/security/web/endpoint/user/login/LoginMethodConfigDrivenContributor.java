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
package org.eulerframework.security.web.endpoint.user.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Supplier;

/**
 * Generic {@link LoginMethodContributor} that iterates the supplied
 * {@link RegisteredLoginMethod}s, delegates each one to the
 * {@link LoginMethodHandler} matching its {@code type}, and returns the
 * resulting flat list of {@link LoginMethod}s.
 *
 * <p>When no registration is supplied, a default {@code password}
 * registration is synthesized so at least one login method is always
 * available.
 *
 * <p>Each registration is exposed under its effective name: the
 * declared {@code method-name}, or the handler-derived default
 * ({@link LoginMethodHandler#resolveName}). The registry key is a
 * storage concern (it may be an opaque id) and is never published.
 * Effective names must be unique; duplicates fail fast.
 *
 * <p>Also exposes a {@link #resolve(String)} method for the routing
 * filter to look up a registered method by its effective name.
 */
public class LoginMethodConfigDrivenContributor implements LoginMethodContributor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, LoginMethodHandler> handlersByType;
    private final Supplier<Collection<RegisteredLoginMethod>> loginMethodsSupplier;

    public LoginMethodConfigDrivenContributor(
            List<LoginMethodHandler> handlers,
            Supplier<Collection<RegisteredLoginMethod>> loginMethodsSupplier) {
        Assert.notNull(handlers, "handlers is required");
        Assert.notNull(loginMethodsSupplier, "loginMethodsSupplier is required");
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
        this.loginMethodsSupplier = loginMethodsSupplier;
    }

    @Override
    public List<LoginMethod> contribute() {
        Map<String, ResolvedLoginMethod> byName = indexByName();
        List<LoginMethod> methods = new ArrayList<>(byName.size());
        for (ResolvedLoginMethod resolved : byName.values()) {
            LoginMethod described = resolved.handler().describe(resolved.name(), resolved.method());
            if (described == null) {
                continue;
            }
            methods.add(described);
        }
        return Collections.unmodifiableList(methods);
    }

    /**
     * Resolves a login method by its effective name for use by the
     * routing filter. Returns {@code null} if the name is unknown.
     */
    public ResolvedLoginMethod resolve(String name) {
        return indexByName().get(name);
    }

    /**
     * Indexes every usable registration by its effective name.
     *
     * @throws IllegalStateException if two registrations resolve to the
     *                               same name
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
            String name = method.getName();
            if (name == null || name.isEmpty()) {
                name = handler.resolveName(method);
            }
            if (name == null || name.isEmpty()) {
                this.logger.warn("Login method '{}' declares no method-name and none could be "
                        + "derived; skipping.", method.getId());
                continue;
            }
            ResolvedLoginMethod previous = index.put(name, new ResolvedLoginMethod(name, method, handler));
            if (previous != null) {
                throw new IllegalStateException("Duplicate login method name '" + name
                        + "' (registrations '" + previous.method().getId() + "' and '"
                        + method.getId() + "'): declare a distinct method-name on one of them.");
            }
        }
        return index;
    }

    private Collection<RegisteredLoginMethod> resolveLoginMethods() {
        Collection<RegisteredLoginMethod> loginMethods = this.loginMethodsSupplier.get();
        if (loginMethods == null || loginMethods.isEmpty()) {
            // Synthesize a default password method so the login page
            // always renders at least the password form.
            return List.of(RegisteredLoginMethod
                    .withId(PasswordLoginMethodHandler.TYPE)
                    .type(PasswordLoginMethodHandler.TYPE)
                    .primary(true)
                    .build());
        }
        return loginMethods;
    }

    /**
     * A registration resolved to its effective name and handler, used
     * by the routing filter to perform dispatch.
     */
    public record ResolvedLoginMethod(String name, RegisteredLoginMethod method, LoginMethodHandler handler) {
    }
}
