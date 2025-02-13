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

package org.eulerframework.socket.netty.channel.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import org.eulerframework.socket.netty.Session;
import org.eulerframework.socket.netty.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class HAProxyInterrupter extends ChannelInboundHandlerAdapter {
    public static final HAProxyInterrupter INSTANCE = new HAProxyInterrupter();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HAProxyMessage haProxyMessage) {
            Session session = Session.getSession(ctx);
            if (session == null) {
                throw new IllegalStateException("No session available, haproxy need a session in this channel.");
            }
            session.setRemoteAddress(haProxyMessage.sourceAddress());
            session.setRemotePort(haProxyMessage.sourcePort());
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
