package net.eulerframework.web.core.exception.web;

import javax.servlet.http.HttpServletRequest;

import net.eulerframework.web.core.exception.WebException;

@SuppressWarnings("serial")
public class PageNotFoundException extends WebException {

    public PageNotFoundException(HttpServletRequest request) {
        super("Page not found: "+request.getRequestURI(), "page_not_found", 404);
    }

}
