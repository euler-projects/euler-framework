package org.eulerframework.socket.annotation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Order()
public @interface NettyHandler {

    /**
     * Alias for {@link Component#value}.
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * Alias for {@link Order#value}.
     */
    @AliasFor(annotation = Order.class, attribute = "value")
    int order() default Ordered.LOWEST_PRECEDENCE;
}
