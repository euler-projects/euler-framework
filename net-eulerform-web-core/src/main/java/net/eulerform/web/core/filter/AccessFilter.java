package net.eulerform.web.core.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import net.eulerform.web.core.log.entity.AccessLog;
import net.eulerform.web.core.util.Log;
import net.eulerform.web.core.util.UrlTool;

public class AccessFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String realURI = UrlTool.findRealURI(request);
		String ip = request.getHeader("X-Real-IP");

		if (ip == null)
			ip = request.getRemoteAddr();

		AccessLog accessLog = new AccessLog();
		accessLog.setAccessDate(new Date());
		accessLog.setClientIp(ip);
		accessLog.setUri(realURI);
		Log.saveAccessLog(accessLog);

		filterChain.doFilter(request, response);
	}	

}
