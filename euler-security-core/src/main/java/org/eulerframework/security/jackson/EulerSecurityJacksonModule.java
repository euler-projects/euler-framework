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
package org.eulerframework.security.jackson;

import org.apache.commons.lang3.ArrayUtils;
import org.eulerframework.security.authentication.WechatAuthorizationCodeAuthenticationToken;
import org.eulerframework.security.core.EulerGrantedAuthority;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.jackson.SecurityJacksonModule;
import org.springframework.security.jackson.SecurityJacksonModules;
import tools.jackson.core.Version;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EulerSecurityJacksonModule extends SecurityJacksonModule {

    public EulerSecurityJacksonModule() {
        super(EulerSecurityJacksonModule.class.getName(), new Version(1, 0, 0, null, null, null));
    }

    @Override
    public void configurePolymorphicTypeValidator(BasicPolymorphicTypeValidator.Builder builder) {
        builder.allowIfSubType(EulerUserDetails.class)
                .allowIfSubType(EulerGrantedAuthority.class)
                .allowIfSubType(WechatAuthorizationCodeAuthenticationToken.class);

    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixIn(EulerUserDetails.class, EulerUserDetailsMixin.class);
        context.setMixIn(EulerGrantedAuthority.class, EulerGrantedAuthorityMixin.class);
        context.setMixIn(WechatAuthorizationCodeAuthenticationToken.class,
                WechatAuthorizationCodeAuthenticationTokenMixin.class);
    }

    public static List<JacksonModule> getModules(ClassLoader loader, JacksonModule... customModules) {

        BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder();

        List<JacksonModule> modules = new ArrayList<>();
        modules.add(new EulerSecurityJacksonModule());

        if (ArrayUtils.isNotEmpty(customModules)) {
            modules.addAll(Arrays.asList(customModules));
        }

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
