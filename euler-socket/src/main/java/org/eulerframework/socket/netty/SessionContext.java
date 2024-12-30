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
}
