package org.eulerframework.socket.dispatcher;

public interface MessageDispatcher<T> {
    Object dispatch(T message);

    void addHandler(MessageHandler messageHandler);
}
