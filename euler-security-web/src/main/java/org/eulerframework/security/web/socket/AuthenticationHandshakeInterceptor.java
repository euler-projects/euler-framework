package org.eulerframework.security.web.socket;

import org.eulerframework.security.core.context.UserContext;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Nonnull;
import java.util.Map;

public class AuthenticationHandshakeInterceptor implements HandshakeInterceptor {
    private final UserContext userContext;

    public final static String ATTR_TENANT_ID = "__requestTenantId";
    public final static String ATTR_USERNAME = "__requestUsername";

    public AuthenticationHandshakeInterceptor(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public boolean beforeHandshake(@Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response, @Nonnull WebSocketHandler wsHandler, @Nonnull Map<String, Object> attributes) throws Exception {
        attributes.put(ATTR_TENANT_ID, this.userContext.getTenantId());
        attributes.put(ATTR_USERNAME, this.userContext.getUsername());
        return true;
    }

    @Override
    public void afterHandshake(@Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response, @Nonnull WebSocketHandler wsHandler, Exception exception) {
        // NOOP
    }
}
