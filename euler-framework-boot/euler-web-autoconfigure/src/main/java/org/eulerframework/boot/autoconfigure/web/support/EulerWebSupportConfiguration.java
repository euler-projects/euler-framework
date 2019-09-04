/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.boot.autoconfigure.web.support;

import org.eulerframework.boot.autoconfigure.web.EulerCacheProperties;
import org.eulerframework.boot.autoconfigure.web.EulerWebProperties;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.core.i18n.ClassPathReloadableResourceBundleMessageSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class EulerWebSupportConfiguration {

    private final EulerWebProperties eulerWebProperties;
    private final EulerCacheProperties eulerCacheProperties;

    public EulerWebSupportConfiguration(EulerWebProperties eulerWebProperties, EulerCacheProperties eulerCacheProperties) {
        this.eulerWebProperties = eulerWebProperties;
        this.eulerCacheProperties = eulerCacheProperties;
    }

    @Bean
    @ConditionalOnClass(ClassPathReloadableResourceBundleMessageSource.class)
    public MessageSource messageSource() {
        ClassPathReloadableResourceBundleMessageSource messageSource = new ClassPathReloadableResourceBundleMessageSource();
        long i18nCacheLifeSeconds = this.eulerCacheProperties.getI18n().getTimeToLive().getSeconds();
        messageSource.setCacheSeconds(i18nCacheLifeSeconds > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) i18nCacheLifeSeconds);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setBasename(this.eulerWebProperties.getI18n().getResourcePath());
        return messageSource;
    }
}
