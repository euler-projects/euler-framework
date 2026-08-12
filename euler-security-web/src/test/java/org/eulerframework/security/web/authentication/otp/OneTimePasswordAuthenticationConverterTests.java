/*
 * Copyright 2013-present the original author or authors.
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
package org.eulerframework.security.web.authentication.otp;

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Conversion behaviour of the one-time-password login submission:
 * only a request carrying both halves of the credential yields a token.
 */
class OneTimePasswordAuthenticationConverterTests {

    private final OneTimePasswordAuthenticationConverter converter =
            new OneTimePasswordAuthenticationConverter();

    @Test
    void completeSubmissionYieldsAnUnauthenticatedToken() {
        Authentication authentication = this.converter.convert(
                request(Map.of("otp_ticket", "ot_2b8f4e", "otp", "123456")));

        OneTimePasswordAuthenticationToken token = (OneTimePasswordAuthenticationToken) authentication;
        assertEquals(false, token.isAuthenticated());
        assertEquals("ot_2b8f4e", token.getPrincipal());
        assertEquals("123456", token.getCredentials());
    }

    @Test
    void missingTicketYieldsNothing() {
        assertNull(this.converter.convert(request(Map.of("otp", "123456"))));
    }

    @Test
    void missingCodeYieldsNothing() {
        assertNull(this.converter.convert(request(Map.of("otp_ticket", "ot_2b8f4e"))));
    }

    @Test
    void blankParametersAreTreatedAsAbsent() {
        assertNull(this.converter.convert(
                request(Map.of("otp_ticket", "  ", "otp", ""))));
    }

    @Test
    void emptyRequestYieldsNothing() {
        assertNull(this.converter.convert(request(Map.of())));
    }

    /**
     * Minimal {@link HttpServletRequest} exposing request parameters only;
     * any other call fails loudly.
     */
    private static HttpServletRequest request(Map<String, String> parameters) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getParameter".equals(method.getName())) {
                        return parameters.get((String) args[0]);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
