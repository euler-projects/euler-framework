package org.eulerframework.security.jackson;

import org.springframework.security.jackson.SecurityJacksonModule;
import org.springframework.security.jackson.SecurityJacksonModules;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.util.ArrayList;
import java.util.List;

public class EulerSecurityJacksonModules {

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
