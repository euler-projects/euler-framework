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
package org.eulerframework.core.function;

import java.util.List;

public abstract class AbstractHandlerChain<H extends Handler<T>, T> implements Handler<T> {
    protected abstract List<H> getHandlers();

    @Override
    public boolean support(T type) {
        return true;
    }

    protected RuntimeException throwHandlerNotFountException(T type) {
        return new RuntimeException("Handler not found for type: " + type);
    }

    protected H getHandler(T type) {
        for (H handler : getHandlers()) {
            if (handler.support(type))
                return handler;
        }

        throw this.throwHandlerNotFountException(type);
    }
}
