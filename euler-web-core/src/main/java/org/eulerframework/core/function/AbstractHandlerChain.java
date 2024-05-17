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
