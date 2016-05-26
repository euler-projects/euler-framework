package net.eulerform.web.core.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.eulerform.web.core.util.UserContext;

import org.apache.logging.log4j.ThreadContext;

public class PostLoggingFilter implements Filter {
    
    @Override
    public void init(FilterConfig paramFilterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest paramServletRequest,
            ServletResponse paramServletResponse, FilterChain paramFilterChain)
            throws IOException, ServletException {
            paramServletRequest.setAttribute("user", UserContext.getCurrentUser());
            ThreadContext.put("username", UserContext.getCurrentUser().getUsername());
        paramFilterChain.doFilter(paramServletRequest, paramServletResponse);
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
