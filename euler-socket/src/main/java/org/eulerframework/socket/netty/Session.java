package org.eulerframework.socket.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);
    private static final AttributeKey<Session> SESSION_KEY = AttributeKey.newInstance("session");

    public static Session getSession(ChannelHandlerContext ctx) {
        return ctx.channel().attr(SESSION_KEY).get();
    }

    public static Session createSession(ChannelHandlerContext ctx) {
        if (getSession(ctx) != null) {
            throw new IllegalStateException("Another session has already been set to this channel context");
        }

        Session session = new Session();
        ctx.channel().attr(SESSION_KEY).set(session);
        return session;
    }

    public static Session removeSession(ChannelHandlerContext ctx) {
        Session session = getSession(ctx);
        if (session != null) {
            ctx.channel().attr(SESSION_KEY).set(null);
        }
        return session;
    }

    private final String sessionId;
    private final Map<String, Object> attributes = new HashMap<>();

    private boolean authenticated;

    public Session() {
        this.sessionId = UUID.randomUUID().toString();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public SessionContext createSessionContext() {
        SessionContext sessionContext = SessionContext.currentContext();
        if (sessionContext != null) {
            LOGGER.trace("Use the existing session context");
            return sessionContext;
        }
        return new SessionContext(this);
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                '}';
    }
}
