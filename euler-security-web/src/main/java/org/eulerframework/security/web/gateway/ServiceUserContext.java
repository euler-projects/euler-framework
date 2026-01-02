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

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.core.context.UserContext;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.web.util.ServletUtils;

import java.util.Optional;
import java.util.function.Function;

public class ServiceUserContext implements UserContext {
    static final String REQUEST_ATTR_NAME = "__EULER_SERVICE_USER_INFO";

    GatewayUserInfo getUserInfo() {
        HttpServletRequest request = ServletUtils.getRequest();
        GatewayUserInfo userInfo = (GatewayUserInfo) request.getAttribute(REQUEST_ATTR_NAME);

        if (userInfo == null) {
            return null;
        }

        return userInfo;
    }

    @Override
    public EulerUserDetails getUserDetails() {
        return Optional.ofNullable(this.getUserInfo())
                .map(userInfo -> EulerUserDetails.builder()
                        .tenantId(userInfo.tenantId())
                        .userId(userInfo.userId())
                        .username(userInfo.username())
                        .password("-")
                        .passwordEncoder(Function.identity())
                        .build())
                .map(userDetails -> {
                    userDetails.eraseCredentials();
                    return userDetails;
                })
                .orElse(null);
    }
}
