package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultViewException extends ViewException {
    public DefaultViewException() {
        super("UNKNOWN_ERROR", -1);
    }
}
