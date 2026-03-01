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

import com.fasterxml.jackson.databind.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EulerSecurityJackson2Modules {
    private static final Log logger = LogFactory.getLog(EulerSecurityJackson2Modules.class);

    private static final List<String> eulerSecurityJackson2ModuleClasses = Arrays.asList(
            "org.eulerframework.security.jackson2.EulerSecurityJackson2Module",
            "org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module");

    public static List<Module> getModules(ClassLoader loader) {

        List<Module> modules = new ArrayList<>();

        // Registering Jackson modules for Euler Security
        for (String className : eulerSecurityJackson2ModuleClasses) {
            addToModulesList(loader, modules, className);
        }

        List<Module> securityModules = SecurityJackson2Modules.getModules(loader);

        modules.addAll(securityModules);
        return modules;
    }

    private static void addToModulesList(ClassLoader loader, List<Module> modules, String className) {
        Module module = loadAndGetInstance(className, loader);
        if (module != null) {
            modules.add(module);
        }
    }

    @SuppressWarnings("unchecked")
    private static Module loadAndGetInstance(String className, ClassLoader loader) {
        try {
            Class<? extends Module> securityModule = (Class<? extends Module>) ClassUtils.forName(className, loader);
            logger.debug(LogMessage.format("Loaded module %s, now registering", className));
            return securityModule.getConstructor().newInstance();
        } catch (Exception ex) {
            logger.debug(LogMessage.format("Cannot load module %s", className), ex);
        }
        return null;
    }
}
