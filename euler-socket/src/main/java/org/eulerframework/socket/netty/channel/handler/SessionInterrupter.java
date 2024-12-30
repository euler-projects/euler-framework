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
