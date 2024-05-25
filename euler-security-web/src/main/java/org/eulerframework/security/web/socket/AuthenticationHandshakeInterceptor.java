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
