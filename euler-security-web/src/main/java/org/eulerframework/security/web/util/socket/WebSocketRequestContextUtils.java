package org.eulerframework.security.web.util.socket;

import org.eulerframework.security.web.socket.AuthenticationHandshakeInterceptor;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketRequestContextUtils {
    public static String getTenantId(WebSocketSession session) {
        return (String) session.getAttributes().get(AuthenticationHandshakeInterceptor.ATTR_TENANT_ID);
    }

    public static String getUsername(WebSocketSession session) {
        return (String) session.getAttributes().get(AuthenticationHandshakeInterceptor.ATTR_USERNAME);
    }
}
