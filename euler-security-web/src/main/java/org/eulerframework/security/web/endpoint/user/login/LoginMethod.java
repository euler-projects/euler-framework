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
package org.eulerframework.security.web.endpoint.user.login;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single entry under
 * {@code euler.security.web.login-methods.<name>}: a discriminator
 * ({@link #getType() type}) plus a free-form
 * {@link #getProperties() properties} bag whose keys are meaningful to
 * the {@link LoginMethodTypeHandler} registered for that type.
 *
 * <p>Kept in the framework layer (not {@code euler-boot-autoconfigure})
 * so that {@link LoginMethodTypeHandler} implementations shipped in
 * feature modules can reference the same type without pulling in the
 * autoconfigure jar. {@code EulerBootSecurityWebProperties} in the
 * boot layer holds the {@code Map<String, LoginMethod>} and binds
 * against Spring Boot's config property machinery.
 */
public class LoginMethod {

    private String type;
    private final Map<String, Object> properties = new LinkedHashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
