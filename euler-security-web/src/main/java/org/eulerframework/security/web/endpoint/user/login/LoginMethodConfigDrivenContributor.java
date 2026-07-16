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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Generic {@link LoginMethodContributor} that iterates the declared
 * {@code euler.security.web.login-methods} map, dispatches each entry
 * to the {@link LoginMethodTypeHandler} matching its {@code type}, and
 * returns the resulting flat list of {@link LoginMethodView}s.
 *
 * <p>Handlers are indexed by {@link LoginMethodTypeHandler#type()} on
 * construction. Entries whose {@code type} has no matching handler are
 * logged at {@code WARN} and skipped (typical when a feature module is
 * not on the classpath, e.g. {@code type: passkey} without the
 * WebAuthn module). Entries whose handler returns {@code null} are
 * also silently skipped &mdash; the handler is expected to have logged
 * the reason.
 *
 * <p>The login-methods map is supplied through a {@link Supplier} so
 * that this bean can be created before the properties object is fully
 * populated (in practice Spring resolves the supplier at first
 * contribute() call).
 */
public class LoginMethodConfigDrivenContributor implements LoginMethodContributor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, LoginMethodTypeHandler> handlersByType;
    private final Supplier<Map<String, LoginMethod>> loginMethodsSupplier;

    public LoginMethodConfigDrivenContributor(
            List<LoginMethodTypeHandler> handlers,
            Supplier<Map<String, LoginMethod>> loginMethodsSupplier) {
        Assert.notNull(handlers, "handlers is required");
        Assert.notNull(loginMethodsSupplier, "loginMethodsSupplier is required");
        Map<String, LoginMethodTypeHandler> index = new HashMap<>(handlers.size());
        for (LoginMethodTypeHandler handler : handlers) {
            String type = handler.type();
            Assert.hasText(type, () -> "LoginMethodTypeHandler#type() returned blank on "
                    + handler.getClass().getName());
            LoginMethodTypeHandler previous = index.put(type, handler);
            if (previous != null) {
                throw new IllegalStateException("Duplicate LoginMethodTypeHandler for type='"
                        + type + "': " + previous.getClass().getName() + " and "
                        + handler.getClass().getName());
            }
        }
        this.handlersByType = Collections.unmodifiableMap(index);
        this.loginMethodsSupplier = loginMethodsSupplier;
    }

    @Override
    public List<LoginMethodView> contribute() {
        Map<String, LoginMethod> loginMethods = this.loginMethodsSupplier.get();
        if (loginMethods == null || loginMethods.isEmpty()) {
            return Collections.emptyList();
        }
        List<LoginMethodView> views = new ArrayList<>(loginMethods.size());
        for (Map.Entry<String, LoginMethod> entry : loginMethods.entrySet()) {
            String name = entry.getKey();
            LoginMethod method = entry.getValue();
            if (method == null || method.getType() == null || method.getType().isEmpty()) {
                this.logger.warn("Login method '{}' has no 'type' declared; skipping.", name);
                continue;
            }
            LoginMethodTypeHandler handler = this.handlersByType.get(method.getType());
            if (handler == null) {
                this.logger.warn("Login method '{}' declares type='{}' but no LoginMethodTypeHandler "
                        + "is registered for that type; skipping. Registered types: {}",
                        name, method.getType(), this.handlersByType.keySet());
                continue;
            }
            LoginMethodView view = handler.toView(name, method.getProperties());
            if (view == null) {
                // Handler already logged the reason; suppress here to
                // avoid duplicate log lines.
                continue;
            }
            views.add(view);
        }
        return Collections.unmodifiableList(views);
    }
}
