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
import java.util.ServiceLoader;

/**
 * Collects the Jackson modules that make up the security {@code JsonMapper}: Euler's own, and
 * Spring Security's.
 *
 * <p>Euler's come from two places. The base module is added outright, because every other module's
 * mixins refer to the types it registers ({@code EulerUserDetails}, {@code UserIdentity}), so losing
 * it to a missing declaration would fail every authorization read rather than one module's. The rest
 * are found through {@link ServiceLoader}, so a module that owns a type it persists &mdash; the
 * authorization server's {@code PublicKeyAuthentication}, say &mdash; registers that type itself and
 * this class never has to name it. Spring Security's own collector works from a hard-coded list of
 * its module class names, which is open to it because they are all its own, and is exactly the
 * coupling this avoids between Euler modules.
 *
 * <p>A contributed module declares itself in
 * {@code META-INF/services/org.springframework.security.jackson.SecurityJacksonModule} and then has
 * to do both halves of the job: {@code configurePolymorphicTypeValidator} decides whether a type id
 * in stored JSON may be resolved at all, {@code setupModule} decides how it is read once it may be.
 * Doing only the second leaves the validator refusing the type before the mixin is ever consulted.
 */
public class EulerSecurityJacksonModules {

    public static List<JacksonModule> getModules(ClassLoader loader) {

        BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder();

        List<JacksonModule> modules = new ArrayList<>();

        // Registering Jackson modules for Euler Security
        modules.add(new EulerSecurityJacksonModule());
        ServiceLoader.load(SecurityJacksonModule.class, loader).forEach(modules::add);

        // Ahead of collecting Spring's modules, because the builder is handed to them: every Euler
        // type has to have been allowed by the time that happens.
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
