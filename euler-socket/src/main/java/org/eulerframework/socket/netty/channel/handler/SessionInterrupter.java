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
import org.eulerframework.socket.netty.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class SessionInterrupter extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(SessionInterrupter.class);
    public static final SessionInterrupter INSTANCE = new SessionInterrupter();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Session session = Session.createSession(ctx);
        this.logger.trace("create new session '{}'", session.getSessionId());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Session session;
        if ((session = Session.removeSession(ctx)) != null) {
            this.logger.trace("remove session '{}'", session.getSessionId());
        }
        super.channelInactive(ctx);
    }
}
