package org.eulerframework.socket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyServer implements ChannelFuture, Runnable, Closeable {
    private final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final int port;
    private final List<ChannelHandler> channelHandlers = new ArrayList<>();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    public NettyServer(int port) {
        this.port = port;
    }

    public void addChannelHandlerAtLast(ChannelHandler... channelHandlers) {
        this.channelHandlers.addAll(Arrays.asList(channelHandlers));
    }

    @Override
    public void close() {
        if (bossGroup != null && !bossGroup.isShutdown()) {
            this.bossGroup.shutdownGracefully();
        }
        if (workerGroup != null && !workerGroup.isShutdown()) {
            this.workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void run() {
        this.bossGroup = new NioEventLoopGroup(); // (1)
        this.workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap(); // (2)
        serverBootstrap.group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class) // (3)
                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(NettyServer.this.channelHandlers.toArray(new ChannelHandler[0]));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

        ChannelFuture f = serverBootstrap.bind(port);
        logger.info("Netty server is starting...");
        try {
            this.channelFuture = f.sync();
        } catch (InterruptedException e) {
            // shutdown groups if thread is interrupted
            this.workerGroup.shutdownGracefully();
            this.bossGroup.shutdownGracefully();

            Thread.currentThread().interrupt();
        }
        logger.info("Netty server is listening on port {}", this.port);
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
}
