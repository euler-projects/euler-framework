/*
 * Copyright 2013-2024 the original author or authors.
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

package org.eulerframework.socket.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionContext.class);
    private static final ThreadLocal<SessionContext> CURRENT_CONTEXT = new ThreadLocal<>();

    public static SessionContext currentContext() {
        return CURRENT_CONTEXT.get();
    }

    public static Session currentSession() {
        return CURRENT_CONTEXT.get().getSession();
    }

    private final Session session;

    SessionContext(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("session is null");
        }
        this.session = session;
        CURRENT_CONTEXT.set(this);
        LOGGER.trace("Session context initialized, session: {}", session.getSessionId());
    }

    public Session getSession() {
        return session;
    }

    public static void clear() {
        CURRENT_CONTEXT.remove();
        LOGGER.trace("Session context has been cleared");
    }
}
