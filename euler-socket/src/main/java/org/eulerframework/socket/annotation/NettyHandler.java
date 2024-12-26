/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eulerframework.socket.annotation;

import org.springframework.context.annotation.Scope;
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
@Scope()
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

    /**
     * Alias for {@link Scope#value}.
     */
    @AliasFor(annotation = Scope.class, attribute = "value")
    String scope() default NettyHandler.SCOPE_PROTOTYPE;

    String SCOPE_SINGLETON = "singleton";
    String SCOPE_PROTOTYPE = "prototype";
}
