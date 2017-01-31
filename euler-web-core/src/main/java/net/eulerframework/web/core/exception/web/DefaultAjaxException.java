package net.eulerframework.web.core.exception.web;

@SuppressWarnings("serial")
public class DefaultAjaxException extends AjaxException {
    public DefaultAjaxException() {
        super("UNKNOWN_ERROR", -1);
    }
}
