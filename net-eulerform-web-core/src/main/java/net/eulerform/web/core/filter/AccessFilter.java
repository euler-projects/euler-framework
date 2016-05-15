package net.eulerform.web.core.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.eulerform.web.core.log.entity.AccessLog;
import net.eulerform.web.core.util.Log;
import net.eulerform.web.core.util.UrlTool;

public class AccessFilter implements Filter {

    @Override
    public void init(FilterConfig paramFilterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest paramServletRequest,
            ServletResponse paramServletResponse, FilterChain paramFilterChain)
            throws IOException, ServletException {
        
        String realURI = UrlTool.findRealURI((HttpServletRequest)paramServletRequest);
        String ip = ((HttpServletRequest)paramServletRequest).getHeader("X-Real-IP");
        
        if(ip == null)
            ip = paramServletRequest.getRemoteAddr();
        
        AccessLog accessLog = new AccessLog();
        accessLog.setAccessDate(new Date());
        accessLog.setClientIp(ip);
        accessLog.setUri(realURI);
        Log.saveAccessLog(accessLog);
        
        paramFilterChain.doFilter(paramServletRequest, paramServletResponse);
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
