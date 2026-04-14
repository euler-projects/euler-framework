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

package org.eulerframework.security.jackson;

import org.springframework.security.jackson.SecurityJacksonModule;
import org.springframework.security.jackson.SecurityJacksonModules;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.util.ArrayList;
import java.util.List;

public class EulerSecurityJacksonModules {

    // TODO multi module support
    private static final String webServletJacksonModuleClass = "org.eulerframework.security.web.jackson.EulerSecurityWebJacksonModule";

    public static List<JacksonModule> getModules(ClassLoader loader) {

        BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder();

        List<JacksonModule> modules = new ArrayList<>();

        // Registering Jackson modules for Euler Security
        modules.add(new EulerSecurityJacksonModule());

        applyPolymorphicTypeValidator(modules, builder);

        List<JacksonModule> securityModules = SecurityJacksonModules.getModules(loader, builder);

        modules.addAll(securityModules);
        return modules;
    }

    private static void applyPolymorphicTypeValidator(List<JacksonModule> modules,
                                                      BasicPolymorphicTypeValidator.Builder typeValidatorBuilder) {
        for (JacksonModule module : modules) {
            if (module instanceof SecurityJacksonModule securityModule) {
                securityModule.configurePolymorphicTypeValidator(typeValidatorBuilder);
            }
        }
    }
}
