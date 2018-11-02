/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.core.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import org.eulerframework.constant.EulerSysAttributes;
import org.eulerframework.web.core.extend.WebLanguageRequestWrapper;
import org.eulerframework.web.core.extend.WebLanguageRequestWrapper.NeedRedirectException;

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
