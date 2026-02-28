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
package org.eulerframework.security.oauth2.server.authorization.jackson;

import org.eulerframework.security.jackson.EulerSecurityJacksonModule;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

public abstract class EulerOAuth2JsonMapper {
    private static JsonMapper jsonMapper;

    public static JsonMapper getInstance() {
        if (jsonMapper != null) {
            return jsonMapper;
        }

        synchronized (EulerOAuth2JsonMapper.class) {
            if (jsonMapper != null) {
                return jsonMapper;
            }

            List<JacksonModule> securityModules = EulerSecurityJacksonModule.getModules(
                    EulerOAuth2JsonMapper.class.getClassLoader(),
                    new OAuth2AuthorizationServerJacksonModule()
            );

            jsonMapper = JsonMapper.builder()
                    .addModules(securityModules)
                    .build();

            return jsonMapper;
        }
    }
}
