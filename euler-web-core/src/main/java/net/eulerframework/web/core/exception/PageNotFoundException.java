package net.eulerframework.web.core.exception;

import javax.servlet.http.HttpServletRequest;

public class PageNotFoundException extends RuntimeException {

    public PageNotFoundException(HttpServletRequest request) {
        super("Page not found: "+request.getRequestURI());
    }

}
