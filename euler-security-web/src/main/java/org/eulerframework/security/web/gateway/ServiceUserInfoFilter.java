/*
 * Copyright 2013-2026 the original author or authors.
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
package org.eulerframework.security.web.gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.json.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServiceUserInfoFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final static byte[] FORBIDDEN_RESPONSE_BODY = "{\"error\": \"Forbidden\", \"message\": \"Invalid User Info\"}".getBytes(StandardCharsets.UTF_8);

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws IOException, ServletException {

        String eulerGatewayUserInfoHeaderValue = request.getHeader(GatewayUserInfoHeaderHelper.GATEWAY_USER_INFO_HEADER_NAME);

        if (!StringUtils.hasText(eulerGatewayUserInfoHeaderValue)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getOutputStream().write(FORBIDDEN_RESPONSE_BODY);
            return;
        }

        GatewayUserInfo gatewayUserInfo;
        try {
            gatewayUserInfo = GatewayUserInfoHeaderHelper.parseHeaderValue(eulerGatewayUserInfoHeaderValue);
            logger.info("User Info parsed success, tenantId: {}, userId {}, username: {}", gatewayUserInfo.tenantId(), gatewayUserInfo.userId(), gatewayUserInfo.username());
        } catch (Exception e) {
            logger.error("Exception thrown while parsing user info from header value '{}'", eulerGatewayUserInfoHeaderValue, e);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getOutputStream().write(FORBIDDEN_RESPONSE_BODY);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
