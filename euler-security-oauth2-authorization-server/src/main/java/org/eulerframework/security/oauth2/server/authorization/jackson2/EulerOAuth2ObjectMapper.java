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
package org.eulerframework.security.oauth2.server.authorization.jackson2;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

import java.util.List;

public abstract class EulerOAuth2ObjectMapper {
    private static ObjectMapper objectMapper;

    public static ObjectMapper getInstance() {
        if (objectMapper != null) {
            return objectMapper;
        }

        synchronized (EulerOAuth2ObjectMapper.class) {
            if (objectMapper != null) {
                return objectMapper;
            }
            objectMapper = new ObjectMapper();
            List<Module> securityModules = SecurityJackson2Modules.getModules(EulerOAuth2ObjectMapper.class.getClassLoader());
            objectMapper.registerModules(securityModules);
            objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
            objectMapper.registerModule(new EulerOAuth2AuthorizationServerJackson2Module());
            return objectMapper;
        }
    }
}
