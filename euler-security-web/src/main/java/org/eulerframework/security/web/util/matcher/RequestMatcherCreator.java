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
package org.eulerframework.security.web.util.matcher;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.ArrayList;
import java.util.List;

public class RequestMatcherCreator {
    private static final String HANDLER_MAPPING_INTROSPECTOR_BEAN_NAME = "mvcHandlerMappingIntrospector";

    private static final String HANDLER_MAPPING_INTROSPECTOR = "org.springframework.web.servlet.handler.HandlerMappingIntrospector";

    private static final boolean mvcPresent;

    static {
        mvcPresent = ClassUtils.isPresent(HANDLER_MAPPING_INTROSPECTOR, RequestMatcherCreator.class.getClassLoader());
    }

    private final ApplicationContext applicationContext;

    public RequestMatcherCreator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public RequestMatcher securityMatcher(String... patterns) {
        if (mvcPresent) {
            return new OrRequestMatcher(createMvcMatchers(patterns));
        }
        return new OrRequestMatcher(createAntMatchers(patterns));
    }

    private List<RequestMatcher> createAntMatchers(String... patterns) {
        List<RequestMatcher> matchers = new ArrayList<>(patterns.length);
        for (String pattern : patterns) {
            matchers.add(new AntPathRequestMatcher(pattern));
        }
        return matchers;
    }

    @SuppressWarnings("unchecked")
    private List<RequestMatcher> createMvcMatchers(String... mvcPatterns) {
        ObjectPostProcessor<Object> opp = this.applicationContext.getBean(ObjectPostProcessor.class);
        if (!this.applicationContext.containsBean(HANDLER_MAPPING_INTROSPECTOR_BEAN_NAME)) {
            throw new NoSuchBeanDefinitionException("A Bean named " + HANDLER_MAPPING_INTROSPECTOR_BEAN_NAME
                    + " of type " + HandlerMappingIntrospector.class.getName()
                    + " is required to use MvcRequestMatcher. Please ensure Spring Security & Spring MVC are configured in a shared ApplicationContext.");
        }
        HandlerMappingIntrospector introspector = this.applicationContext.getBean(HANDLER_MAPPING_INTROSPECTOR_BEAN_NAME,
                HandlerMappingIntrospector.class);
        List<RequestMatcher> matchers = new ArrayList<>(mvcPatterns.length);
        for (String mvcPattern : mvcPatterns) {
            MvcRequestMatcher matcher = new MvcRequestMatcher(introspector, mvcPattern);
            opp.postProcess(matcher);
            matchers.add(matcher);
        }
        return matchers;
    }
}
