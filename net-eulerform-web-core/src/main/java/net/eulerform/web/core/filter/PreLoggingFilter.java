package net.eulerform.web.core.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.filter.OncePerRequestFilter;

public class PreLoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String id = UUID.randomUUID().toString();
		ThreadContext.put("id", id);
		try {
			filterChain.doFilter(request, response);
		} finally {
			ThreadContext.remove("id");
			ThreadContext.remove("username");
		}
	}

}
