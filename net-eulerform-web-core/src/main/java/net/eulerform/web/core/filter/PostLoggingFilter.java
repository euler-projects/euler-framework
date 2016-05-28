package net.eulerform.web.core.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.filter.OncePerRequestFilter;

import net.eulerform.web.core.security.authentication.util.UserContext;

public class PostLoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		request.setAttribute("currentUser", UserContext.getCurrentUser());
		request.getServletContext().setAttribute("currentUser", UserContext.getCurrentUser());
		ThreadContext.put("username", UserContext.getCurrentUser().getUsername());
		filterChain.doFilter(request, response);
	}

}
