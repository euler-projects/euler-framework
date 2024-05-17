package org.eulerframework.core.function;

public interface Handler<T> {
    boolean support(T type);
}
