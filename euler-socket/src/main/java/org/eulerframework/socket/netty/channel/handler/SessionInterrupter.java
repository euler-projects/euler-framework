package org.eulerframework.socket.netty.channel.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.eulerframework.socket.netty.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class SessionInterrupter extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(SessionInterrupter.class);
    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.newInstance("session");
    public static final SessionInterrupter INSTANCE = new SessionInterrupter();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Attribute<Session> attr = ctx.channel().attr(SESSION_KEY);
        if (attr.get() != null) {
            throw new IllegalStateException("Another session has already been set to this channel context");
        }
        Session session = new Session();
        this.logger.trace("create new session '{}'", session.getSessionId());
        attr.set(session);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Attribute<Session> attr = ctx.channel().attr(SESSION_KEY);
        Session session;
        if ((session = attr.get()) != null) {
            this.logger.trace("remove session '{}'", session.getSessionId());
            attr.set(null);
        }
        super.channelInactive(ctx);
    }
}
