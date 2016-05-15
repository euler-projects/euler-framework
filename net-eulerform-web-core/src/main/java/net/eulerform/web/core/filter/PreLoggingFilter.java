package net.eulerform.web.core.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.logging.log4j.ThreadContext;

public class PreLoggingFilter implements Filter {
    
    @Override
    public void init(FilterConfig paramFilterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest paramServletRequest,
            ServletResponse paramServletResponse, FilterChain paramFilterChain)
            throws IOException, ServletException {
        
        String id = UUID.randomUUID().toString();
        ThreadContext.put("id", id);
        try {
            paramFilterChain.doFilter(paramServletRequest, paramServletResponse);
        } finally {
            ThreadContext.remove("id");
            ThreadContext.remove("username");
        }
//        boolean clear = false;
//        if(!ThreadContext.containsKey("id")) {
//            clear = true;
//            ThreadContext.put("id", UUID.randomUUID().toString());
//            SecurityContext context = SecurityContextHolder.getContext();
//            if(context != null && context.getAuthentication() != null){
//                ThreadContext.put("username", context.getAuthentication().getName());
//            }
//        }
//        try {
//            paramFilterChain.doFilter(paramServletRequest, paramServletResponse);
//        } finally {
//            if(clear)
//                ThreadContext.clearAll();
//        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
