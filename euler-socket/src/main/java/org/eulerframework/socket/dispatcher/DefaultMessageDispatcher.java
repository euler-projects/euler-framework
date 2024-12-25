package org.eulerframework.socket.dispatcher;

import java.util.ArrayList;
import java.util.List;

public class DefaultMessageDispatcher implements MessageDispatcher<Object> {
    private final List<MessageHandler> handlers = new ArrayList<>();

    @Override
    public Object dispatch(Object message) {
        for (MessageHandler handler : handlers) {
            if (handler.support(message)) {
                return handler.handle(message);
            }
        }
        throw new IllegalArgumentException("No message handler was found for this message");
    }

    @Override
    public void addHandler(MessageHandler messageHandler) {
        this.handlers.add(messageHandler);
    }
}
