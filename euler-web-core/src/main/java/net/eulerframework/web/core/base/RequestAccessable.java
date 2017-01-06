package net.eulerframework.web.core.base;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class RequestAccessable {
    
    protected ServletContext getServletContext(){
        return RequestContext.getServletContext();
    }
    
    protected HttpServletRequest getRequest() {
        return RequestContext.getRequest();
    }
    
    protected HttpServletResponse getResponse() {
        return RequestContext.getResponse();
    }

}
