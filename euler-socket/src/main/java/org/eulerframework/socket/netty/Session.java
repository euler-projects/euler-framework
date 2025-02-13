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

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;

public interface Session {
    String getSessionId();

    String getRemoteAddress();

    int getRemotePort();

    void addAttribute(String key, Object value);

    Object getAttribute(String key);

    Map<String, Object> getAttributes();

    void setAuthenticated(boolean authenticated);

    boolean isAuthenticated();

    default SessionContext createSessionContext() {
        SessionContext sessionContext = SessionContext.currentContext();
        if (sessionContext != null) {
            LOGGER.trace("Use the existing session context");
            return sessionContext;
        }
        return new SessionContext(this);
    }

    Logger LOGGER = LoggerFactory.getLogger(Session.class);
    AttributeKey<Session> SESSION_KEY = AttributeKey.newInstance("session");

    static Session getSession(ChannelHandlerContext ctx) {
        return ctx.channel().attr(SESSION_KEY).get();
    }

    static Session createSession(ChannelHandlerContext ctx) {
        if (getSession(ctx) != null) {
            throw new IllegalStateException("Another session has already been set to this channel context");
        }

        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        Session session = new DefaultSession(remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());

        ctx.channel().attr(SESSION_KEY).set(session);
        return session;
    }

    static Session removeSession(ChannelHandlerContext ctx) {
        Session session = getSession(ctx);
        if (session != null) {
            ctx.channel().attr(SESSION_KEY).set(null);
        }
        return session;
    }
}
