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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class GatewayUserInfoHeaderHelperTest {

    private final static String TEST_HEADER_VALUE = // "eyJ0ZW5hbnRJZCI6InQxIiwidXNlcklkIjoidTEiLCJ1c2VybmFtZSI6Im5hbWUifQ==";
            Base64.getEncoder().encodeToString("{\"tenantId\":\"t1\",\"userId\":\"u1\",\"username\":\"name\"}".getBytes(StandardCharsets.UTF_8));
    private final static GatewayUserInfo TEST_INFO = new GatewayUserInfo("t1", "u1", "name");

    @Test
    void toHeaderValue() {
        String headerValue = GatewayUserInfoHeaderHelper.toHeaderValue(TEST_INFO);
        Assertions.assertEquals(TEST_HEADER_VALUE, headerValue);
    }

    @Test
    void parseHeaderValue() {
        GatewayUserInfo parsedUserInfo = GatewayUserInfoHeaderHelper.parseHeaderValue(TEST_HEADER_VALUE);
        Assertions.assertEquals(TEST_INFO.tenantId(), parsedUserInfo.tenantId());
        Assertions.assertEquals(TEST_INFO.userId(), parsedUserInfo.userId());
        Assertions.assertEquals(TEST_INFO.username(), parsedUserInfo.username());
    }
}