package net.eulerform.web.core.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class CrosFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
	    
	    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
	    response.setHeader("Access-Control-Allow-Methods", request.getHeader("Access-Control-Request-Method"));
        response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
	    response.setHeader("Access-Control-Max-Age", "1800");
        response.setHeader("Access-Control-Allow-Credentials", "true");
	    
	    filterChain.doFilter(request, response);
	}

}
