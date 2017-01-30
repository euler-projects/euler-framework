package net.eulerframework.web.core.base;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import net.eulerframework.common.util.log.LogSupport;


public abstract class WebContextAccessable extends LogSupport {
    
    protected ServletContext getServletContext(){
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();  
        return webApplicationContext.getServletContext();
    }
    
    private ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    }
    
    protected HttpServletRequest getRequest() {
        return this.getServletRequestAttributes().getRequest();
    }
    
    protected HttpServletResponse getResponse() {
        return this.getServletRequestAttributes().getResponse();
    }

}
