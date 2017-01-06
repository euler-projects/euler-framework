package net.eulerframework.web.core.base;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public abstract class RequestContext {
    
    public static ServletContext getServletContext(){
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();  
        return webApplicationContext.getServletContext();
    }
    
    private static ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    }
    
    public static HttpServletRequest getRequest() {
        return getServletRequestAttributes().getRequest();
    }
    
    public static HttpServletResponse getResponse() {
        return getServletRequestAttributes().getResponse();
    }

}
