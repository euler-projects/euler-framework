package net.eulerframework.web.util;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ServletUtils {
    
    public static ServletContext getServletContext(){
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();  
        return webApplicationContext.getServletContext();
    }
    
    public static ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    }
    
    public static HttpServletRequest getRequest() {
        return getServletRequestAttributes().getRequest();
    }
    
    public static HttpServletResponse getResponse() {
        return getServletRequestAttributes().getResponse();
    }
    
    public static String getWebDomain(){
        HttpServletRequest request = getRequest();
        StringBuffer url = request.getRequestURL();
        String uri = request.getRequestURI();
        return url.delete(url.length() - uri.length(), url.length()).toString();
    }
    
    public static String findRealURI(HttpServletRequest httpServletRequest) {        
        String requestURI = httpServletRequest.getRequestURI();
        String contextPath = httpServletRequest.getContextPath();        
        return requestURI.replaceFirst(contextPath, "").trim();
    }
}
