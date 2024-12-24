package org.eulerframework.socket.dispatcher;

import java.lang.reflect.Method;

public interface MessageHandlerBuilder {
    MessageHandler build(Object socketController, Method mappingMethod);
}
