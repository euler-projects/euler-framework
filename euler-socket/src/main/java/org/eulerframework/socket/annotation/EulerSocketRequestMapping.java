package org.eulerframework.socket.annotation;

import org.eulerframework.socket.dispatcher.MessageHandlerBuilder;

import java.lang.annotation.*;

@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EulerSocketRequestMapping {
    Class<? extends MessageHandlerBuilder> handlerCreator();
}
