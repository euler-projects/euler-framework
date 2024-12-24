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

package org.eulerframework.socket.configurers;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.eulerframework.common.util.JavaObjectUtils;
import org.eulerframework.socket.annotation.EulerSocketController;
import org.eulerframework.socket.annotation.EulerSocketRequestMapping;
import org.eulerframework.socket.dispatcher.MessageDispatcher;
import org.eulerframework.socket.dispatcher.MessageHandler;
import org.eulerframework.socket.dispatcher.MessageHandlerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EulerSocketServerConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(EulerSocketServerConfiguration.class);

    public static void setupMessageDispatcher(MessageDispatcher<?> messageDispatcher, ApplicationContext applicationContext) {
        Map<String, Object> socketControllers = applicationContext.getBeansWithAnnotation(EulerSocketController.class);
        if (CollectionUtils.isEmpty(socketControllers)) {
            return;
        }
        setupMessageDispatcher(messageDispatcher, socketControllers.values());
    }

    public static void setupMessageDispatcher(MessageDispatcher<?> messageDispatcher, Collection<Object> socketControllers) {
        for (Object socketController : socketControllers) {
            List<Method> socketMappingMethods = MethodUtils.getMethodsListWithAnnotation(
                    socketController.getClass(), EulerSocketRequestMapping.class);

            for (Method method : socketMappingMethods) {
                EulerSocketRequestMapping socketRequestMapping = method.getAnnotation(EulerSocketRequestMapping.class);
                Class<? extends MessageHandlerBuilder> handlerCreatorClass = socketRequestMapping.handlerCreator();
                MessageHandlerBuilder messageHandlerBuilder = JavaObjectUtils.newInstance(handlerCreatorClass);
                MessageHandler messageHandler = messageHandlerBuilder.build(socketController, method);
                messageDispatcher.addHandler(messageHandler);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Registered socket message handler for '{}'", method.toGenericString());
                }
            }
        }
    }

}
