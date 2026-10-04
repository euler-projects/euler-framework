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

package org.eulerframework.security.oauth2.server.authorization.web.authentication;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal {@link HttpServletRequest} stubs backed by maps, shared by the client attestation tests. A
 * dynamic proxy avoids pulling in a servlet test dependency: only what those tests reach is
 * answered, and any other reference-typed accessor falls through to {@code null}.
 * <p>
 * {@code getParameterMap}, {@code getQueryString} and {@code getMethod} are answered, not just
 * {@code getParameter}, because Spring's {@code OAuth2EndpointUtils#getFormParameters} reads the
 * parameter map and filters it by the query string. A stub answering only {@code getParameter} would
 * let grant-parameter collection fail with a {@code NullPointerException} rather than test anything.
 */
final class TestHttpServletRequests {

    /**
     * A POST token endpoint request carrying the given headers and form parameters.
     *
     * @param headers    header name to value
     * @param parameters form parameter name to value, single-valued
     * @return the stubbed request
     */
    static HttpServletRequest post(Map<String, String> headers, Map<String, String> parameters) {
        Map<String, String[]> parameterMap = new HashMap<>();
        parameters.forEach((key, value) -> parameterMap.put(key, new String[]{value}));

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getHeader" -> headers.get((String) args[0]);
                    case "getParameter" -> parameters.get((String) args[0]);
                    case "getParameterMap" -> parameterMap;
                    case "getQueryString" -> null;
                    case "getMethod" -> "POST";
                    case "toString" -> "HttpServletRequestStub";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> primitiveDefault(method.getReturnType());
                });
    }

    private static Object primitiveDefault(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == char.class) {
            return (char) 0;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        return null;
    }

    private TestHttpServletRequests() {
    }
}
