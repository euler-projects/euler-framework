package org.eulerframework.socket.netty;

public record SessionContext(Session session) {
    private static final ThreadLocal<SessionContext> CURRENT_CONTEXT = new ThreadLocal<>();

    public static SessionContext currentContext() {
        return CURRENT_CONTEXT.get();
    }

    public SessionContext(Session session) {
        this.session = session;
        CURRENT_CONTEXT.set(this);
    }
}
