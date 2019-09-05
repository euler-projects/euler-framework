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
package org.eulerframework.boot.autoconfigure;

import org.eulerframework.boot.autoconfigure.web.EulerCacheProperties;
import org.eulerframework.boot.autoconfigure.web.EulerApplicationProperties;
import org.eulerframework.boot.autoconfigure.web.EulerWebProperties;
import org.eulerframework.boot.autoconfigure.web.support.EulerWebSupportConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties({EulerApplicationProperties.class, EulerCacheProperties.class, EulerWebProperties.class})
@Import({EulerWebSupportConfiguration.class})
public class EulerWebAutoConfiguration {
}
