package net.eulerframework.web.core.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public abstract class ExcluedableFilter extends OncePerRequestFilter {
    
    private String[] excludeServletPath;

    public void setExcludeServletPath(String[] excludeServletPath) {
        this.excludeServletPath = excludeServletPath;
    }

    public ExcluedableFilter() {
        super();
    }
    
    public ExcluedableFilter(String... excludeServletPath) {
        super();
        this.excludeServletPath = excludeServletPath;        
    }

    @Override
    protected final void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String servletPath = request.getServletPath();
        
        if(excludeServletPath  != null) {
            for(String each : excludeServletPath) {
                if(servletPath.endsWith(each)){
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
        
        this.doFilterIn(request, response, filterChain);
    }
    
    protected abstract void doFilterIn(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException;
}
