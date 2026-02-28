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

import org.eulerframework.common.util.json.jackson3.JacksonUtils;

import java.util.Base64;

public class GatewayUserInfoHeaderHelper {
    public final static String GATEWAY_USER_INFO_HEADER_NAME = "Euler-UserInfo";

    public static String toHeaderValue(GatewayUserInfo gatewayUserInfo) {
        return Base64.getEncoder().encodeToString(JacksonUtils.writeValueAsBytes(gatewayUserInfo));
    }

    public static GatewayUserInfo parseHeaderValue(String headerValue) {
        return JacksonUtils.readValue(Base64.getDecoder().decode(headerValue), GatewayUserInfo.class);
    }
}
