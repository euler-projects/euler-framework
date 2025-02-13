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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import jakarta.annotation.Nonnull;
import org.eulerframework.socket.dispatcher.MessageDispatcher;
import org.eulerframework.socket.netty.channel.handler.HAProxyInterrupter;
import org.eulerframework.socket.netty.channel.handler.MessageDispatcherHandler;
import org.eulerframework.socket.netty.channel.handler.SessionInterrupter;
import org.eulerframework.socket.netty.channel.handler.SessionSupportMessageDispatcherHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NettyServer implements ChannelFuture, Runnable, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    private final int port;
    private final ChannelHandler channelInitializer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    private NettyServer(int port, ChannelHandler channelInitializer) {
        Assert.isTrue(port > 0 && port < 65538, "invalid server port: " + port);
        Assert.notNull(channelInitializer, "channelHandlers must not be null");
        this.port = port;
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void close() {
        LOGGER.info("Netty server is closing...");
        if (bossGroup != null && !bossGroup.isShutdown()) {
            this.bossGroup.shutdownGracefully();
        }
        if (workerGroup != null && !workerGroup.isShutdown()) {
            this.workerGroup.shutdownGracefully();
        }
        LOGGER.info("Netty server closed.");
    }

    @Override
    public void run() {
        this.bossGroup = new NioEventLoopGroup(); // (1)
        this.workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap(); // (2)
        serverBootstrap.group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class) // (3)
                .childHandler(this.channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

        ChannelFuture f = serverBootstrap.bind(port);
        LOGGER.info("Netty server is starting...");
        try {
            this.channelFuture = f.sync();
        } catch (InterruptedException e) {
            // shutdown groups if thread is interrupted
            this.workerGroup.shutdownGracefully();
            this.bossGroup.shutdownGracefully();

            Thread.currentThread().interrupt();
        }
        LOGGER.info("Netty server is listening on port {}", this.port);
    }

    @Override
    public Channel channel() {
        return this.channelFuture.channel();
    }

    @Override
    public boolean isSuccess() {
        return this.channelFuture.isSuccess();
    }

    @Override
    public boolean isCancellable() {
        return this.channelFuture.isCancellable();
    }

    @Override
    public Throwable cause() {
        return this.channelFuture.cause();
    }

    @Override
    public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        return this.channelFuture.addListener(listener);
    }

    @Override
    @SafeVarargs
    public final ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        return this.channelFuture.addListeners(listeners);
    }

    @Override
    public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        return this.channelFuture.removeListener(listener);
    }

    @Override
    @SafeVarargs
    public final ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        return this.channelFuture.removeListeners(listeners);
    }

    @Override
    public ChannelFuture sync() throws InterruptedException {
        return this.channelFuture.sync();
    }

    @Override
    public ChannelFuture syncUninterruptibly() {
        return this.channelFuture.syncUninterruptibly();
    }

    @Override
    public ChannelFuture await() throws InterruptedException {
        return this.channelFuture.await();
    }

    @Override
    public ChannelFuture awaitUninterruptibly() {
        return this.channelFuture.awaitUninterruptibly();
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return this.channelFuture.await(timeout, unit);
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return this.channelFuture.await(timeoutMillis);
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return this.channelFuture.awaitUninterruptibly(timeout, unit);
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return this.channelFuture.awaitUninterruptibly(timeoutMillis);
    }

    @Override
    public Void getNow() {
        return this.channelFuture.getNow();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.channelFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.channelFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.channelFuture.isDone();
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        return this.channelFuture.get();
    }

    @Override
    public Void get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.channelFuture.get(timeout, unit);
    }

    @Override
    public boolean isVoid() {
        return this.channelFuture.isVoid();
    }

    public static class Builder {
        private int port;
        private boolean sessionEnabled = false;
        private boolean haproxyEnabled = false;
        private MessageDispatcher<?, ?> messageDispatcher;
        private final List<Consumer<ChannelPipeline>> channelHandlerAppender = new ArrayList<>();

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder enableSession() {
            this.sessionEnabled = true;
            return this;
        }

        public Builder enableHAProxy() {
            this.haproxyEnabled = true;
            return this;
        }

        public Builder messageDispatcher(MessageDispatcher<?, ?> messageDispatcher) {
            this.messageDispatcher = messageDispatcher;
            return this;
        }

//        public Builder addChannelHandlerAppenderAtLast(Consumer<ChannelPipeline> channelHandlerAppender) {
//            this.channelHandlerAppender.add(channelHandlerAppender);
//            return this;
//        }

        public Builder addChannelHandlersAtLast(Supplier<ChannelHandler[]> channelHandlerSupplier) {
            this.channelHandlerAppender.add(pipeline -> pipeline.addLast(channelHandlerSupplier.get()));
            return this;
        }

        public Builder addChannelHandlerAtLast(Supplier<ChannelHandler> channelHandlerSupplier) {
            this.channelHandlerAppender.add(pipeline -> pipeline.addLast(channelHandlerSupplier.get()));
            return this;
        }

        public NettyServer build() {
            final MessageDispatcherHandler<?, ?> messageDispatcherHandler = this.sessionEnabled ?
                    SessionSupportMessageDispatcherHandler.newInstance(this.messageDispatcher) :
                    MessageDispatcherHandler.newInstance(this.messageDispatcher);
            ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    long begin = System.currentTimeMillis();

                    ChannelPipeline pipeline = ch.pipeline();

                    if (Builder.this.sessionEnabled) {
                        pipeline.addLast(SessionInterrupter.INSTANCE);
                    }

                    if (Builder.this.haproxyEnabled) {
                        pipeline.addLast(new HAProxyMessageDecoder());
                        pipeline.addLast(HAProxyInterrupter.INSTANCE);
                    }

                    Builder.this.channelHandlerAppender.forEach(appender -> appender.accept(pipeline));
                    pipeline.addLast(messageDispatcherHandler);

                    LOGGER.info("Init channel pipeline cost {} ms", System.currentTimeMillis() - begin);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("The channel pipeline info: {}", pipeline.toMap());
                    }
                }
            };
            return new NettyServer(this.port, channelInitializer);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
