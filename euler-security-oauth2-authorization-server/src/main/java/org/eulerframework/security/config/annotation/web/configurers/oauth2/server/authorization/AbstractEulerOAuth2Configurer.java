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
package org.eulerframework.security.config.annotation.web.configurers.oauth2.server.authorization;

import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Base configurer for one of this framework's authorization server endpoints. It mirrors the shape of
 * Spring's {@code AbstractOAuth2Configurer}, which is package-private to
 * {@code org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization} and
 * therefore cannot be extended from here.
 *
 * @see EulerOAuth2AuthorizationServerConfigurer
 */
abstract class AbstractEulerOAuth2Configurer {

    private final ObjectPostProcessor<Object> objectPostProcessor;

    AbstractEulerOAuth2Configurer(ObjectPostProcessor<Object> objectPostProcessor) {
        this.objectPostProcessor = objectPostProcessor;
    }

    abstract void init(HttpSecurity httpSecurity);

    abstract void configure(HttpSecurity httpSecurity);

    abstract RequestMatcher getRequestMatcher();

    protected final <T> T postProcess(T object) {
        return (T) this.objectPostProcessor.postProcess(object);
    }
}
