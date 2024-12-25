package org.eulerframework.socket.netty.channel.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.eulerframework.socket.dispatcher.MessageDispatcher;

@ChannelHandler.Sharable
public class MessageDispatcherHandler<T> extends ChannelInboundHandlerAdapter { // (1)
    public static <T> MessageDispatcherHandler<T> newInstance(MessageDispatcher<T> messageDispatcher) {
        return new MessageDispatcherHandler<>(messageDispatcher);
    }

    private final MessageDispatcher<T> messageDispatcher;

    private MessageDispatcherHandler(MessageDispatcher<T> messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        ctx
                .writeAndFlush(this.messageDispatcher.dispatch((T) msg))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
