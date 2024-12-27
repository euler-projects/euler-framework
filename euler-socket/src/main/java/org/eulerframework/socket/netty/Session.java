package org.eulerframework.socket.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Session {
    private final String sessionId;
    private boolean authenticated;
    private final Map<String, Object> attributes = new HashMap<>();

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

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                '}';
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
