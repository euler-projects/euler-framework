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
import io.netty.handler.codec.haproxy.HAProxyMessage;

import java.util.Map;

public class HAProxySession implements Session {
    public static Session wrapSession(ChannelHandlerContext ctx, HAProxyMessage haProxyMessage) {
        Session session = Session.getSession(ctx);
        if (session == null) {
            throw new IllegalStateException("No session available, haproxy need a session in this channel.");

        }
        HAProxySession haproxySession = new HAProxySession(session, haProxyMessage);
        ctx.channel().attr(SESSION_KEY).set(haproxySession);
        return session;
    }

    private final Session wrappedSession;
    private final String remoteAddress;
    private final int remotePort;

    private HAProxySession(Session wrappedSession, HAProxyMessage haProxyMessage) {
        this.wrappedSession = wrappedSession;
        this.remoteAddress = haProxyMessage.sourceAddress();
        this.remotePort = haProxyMessage.sourcePort();
    }

    @Override
    public String getSessionId() {
        return wrappedSession.getSessionId();
    }

    @Override
    public String getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    @Override
    public void addAttribute(String key, Object value) {
        wrappedSession.addAttribute(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return wrappedSession.getAttribute(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return wrappedSession.getAttributes();
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        wrappedSession.setAuthenticated(authenticated);
    }

    @Override
    public boolean isAuthenticated() {
        return wrappedSession.isAuthenticated();
    }

    @Override
    public String toString() {
        return "HAProxySession{" +
                "wrappedSession=" + wrappedSession +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", remotePort=" + remotePort +
                '}';
    }
}
