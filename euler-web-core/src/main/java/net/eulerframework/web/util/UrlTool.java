package net.eulerframework.web.util;

import javax.servlet.http.HttpServletRequest;

public class UrlTool {
    
    public static String findRealURI(HttpServletRequest httpServletRequest) {        
        String requestURI = httpServletRequest.getRequestURI();
        String contextPath = httpServletRequest.getContextPath();        
        return requestURI.replaceFirst(contextPath, "").trim();
    }
}
