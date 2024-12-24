package org.eulerframework.socket.dispatcher;

public interface MessageHandler {
    Object handle(Object value);
}
