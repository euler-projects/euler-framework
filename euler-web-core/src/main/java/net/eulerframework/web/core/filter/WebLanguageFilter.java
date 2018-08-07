package net.eulerframework.web.core.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import net.eulerframework.constant.EulerSysAttributes;
import net.eulerframework.web.core.extend.WebLanguageRequestWrapper;
import net.eulerframework.web.core.extend.WebLanguageRequestWrapper.NeedRedirectException;

public class WebLanguageFilter extends OncePerRequestFilter {

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
	    WebLanguageRequestWrapper localeRequest;
        try {
            localeRequest = new WebLanguageRequestWrapper(request, response);
        } catch (NeedRedirectException e) {
            response.sendRedirect(e.getRedirectUrl());
            return;
        }
	    localeRequest.setAttribute(EulerSysAttributes.LOCALE.value(), localeRequest.getLocale());
		filterChain.doFilter(localeRequest, response);
	}
	
	/**
	 * 返回false, 否则访问错误页面不会显示为对应语言
	 */
	@Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

}
