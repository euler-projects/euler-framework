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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.eulerframework.socket.dispatcher.MessageDispatcher;
import org.eulerframework.socket.netty.Session;
import org.eulerframework.socket.netty.SessionContext;

@ChannelHandler.Sharable
public class SessionSupportMessageDispatcherHandler<T> extends MessageDispatcherHandler<T> {

    public static <T> SessionSupportMessageDispatcherHandler<T> newInstance(MessageDispatcher<T> messageDispatcher) {
        return new SessionSupportMessageDispatcherHandler<>(messageDispatcher);
    }

    private SessionSupportMessageDispatcherHandler(MessageDispatcher<T> messageDispatcher) {
        super(messageDispatcher);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Session session = Session.getSession(ctx);
        if (session == null) {
            throw new IllegalStateException("There is no session in the channel context");
        }
        SessionContext ignore = session.createSessionContext();
        super.channelRead(ctx, msg);
    }
}
