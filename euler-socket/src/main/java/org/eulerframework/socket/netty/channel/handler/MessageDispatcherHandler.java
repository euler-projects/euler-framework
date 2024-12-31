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

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.eulerframework.socket.dispatcher.MessageDispatcher;

@ChannelHandler.Sharable
public class MessageDispatcherHandler<IN, OUT> extends ChannelInboundHandlerAdapter {

    public static <IN, OUT> MessageDispatcherHandler<IN, OUT> newInstance(MessageDispatcher<IN, OUT> messageDispatcher) {
        return new MessageDispatcherHandler<>(messageDispatcher);
    }

    private final MessageDispatcher<IN, OUT> messageDispatcher;

    protected MessageDispatcherHandler(MessageDispatcher<IN, OUT> messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx
                .writeAndFlush(this.messageDispatcher.dispatch((IN) msg))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
