/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.module.authentication.extend;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.util.StringUtils;

public class EulerUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String failureParam="authentication_failed";

	public EulerUrlAuthenticationFailureHandler() {
	}

	public EulerUrlAuthenticationFailureHandler(String failureParam) {
		setFailureParam(failureParam);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {

		saveException(request, exception);
		
		String requestUrl = request.getRequestURI().substring(request.getContextPath().length());
        String queryString = request.getQueryString();
        
        if(StringUtils.hasText(queryString)) { 
            if(!queryString.endsWith(failureParam)) {
                queryString += ("&" + failureParam);
            }
        } else {
            queryString = failureParam;
        }
        
        String redirectUrl = requestUrl + "?" + queryString;

		if (isUseForward()) {
			logger.debug("Forwarding to " + redirectUrl);

			request.getRequestDispatcher(redirectUrl)
					.forward(request, response);
		}
		else {
			logger.debug("Redirecting to " + redirectUrl);
			getRedirectStrategy().sendRedirect(request, response, redirectUrl);
		}
	}

    public void setFailureParam(String failureParam) {
        this.failureParam = failureParam;
    }
	
}
