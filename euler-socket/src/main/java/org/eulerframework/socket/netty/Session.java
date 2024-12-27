package org.eulerframework.socket.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Session {
    private final String sessionId;
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

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                '}';
    }
}
