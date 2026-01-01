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
package org.eulerframework.security.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.jackson2.CoreJackson2Module;

public abstract class EulerSecurityObjectMapper {
    private static ObjectMapper objectMapper;

    public static ObjectMapper getInstance() {
        if (objectMapper != null) {
            return objectMapper;
        }

        synchronized (EulerSecurityObjectMapper.class) {
            if (objectMapper != null) {
                return objectMapper;
            }
            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new CoreJackson2Module());
            objectMapper.registerModule(new EulerSecurityJackson2Module());
            return objectMapper;
        }
    }
}
