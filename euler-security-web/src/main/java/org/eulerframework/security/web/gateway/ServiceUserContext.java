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
import org.eulerframework.web.util.ServletUtils;

import java.util.Optional;

public class ServiceUserContext {
    static final String REQUEST_ATTR_NAME = "__EULER_SERVICE_USER_INFO";

    public GatewayUserInfo getUserDetails() {
        HttpServletRequest request = ServletUtils.getRequest();
        return (GatewayUserInfo) request.getAttribute(REQUEST_ATTR_NAME);
    }

    public String getUserId() {
        return Optional.ofNullable(getUserDetails()).map(GatewayUserInfo::userId).orElse(null);
    }

    public String getTenantId() {
        return Optional.ofNullable(getUserDetails()).map(GatewayUserInfo::tenantId).orElse(null);
    }
}
