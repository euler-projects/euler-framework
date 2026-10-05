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
package org.eulerframework.security.oauth2.server.authorization.jackson;

import org.eulerframework.security.oauth2.server.authorization.authentication.PublicKeyAuthentication;
import org.springframework.security.jackson.SecurityJacksonModule;
import tools.jackson.core.Version;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Contributes the authorization server's own types to the security {@code JsonMapper}: what it may
 * deserialize, and the mixins telling it how.
 *
 * <p>Discovered through {@link java.util.ServiceLoader} by
 * {@code org.eulerframework.security.jackson.EulerSecurityJacksonModules}, on the strength of this
 * module's {@code META-INF/services} declaration. That is what lets a type this grant persists live
 * here rather than in the module that assembles the mapper: the assembler names no sibling module,
 * so nothing outside this one has to know these types exist.
 *
 * <p>Every {@code Authentication} a grant provider writes into an authorization's
 * {@code java.security.Principal} attribute has to be registered here, together with every
 * non-final type it holds. The authorization store reads the attribute map back in one pass, so one
 * unregistered type fails the read outright rather than degrading a single field &mdash; and takes
 * unrelated endpoints such as OIDC UserInfo down with it.
 *
 * @see PublicKeyAuthentication
 */
public class EulerAuthorizationServerJacksonModule extends SecurityJacksonModule {

    public EulerAuthorizationServerJacksonModule() {
        super(EulerAuthorizationServerJacksonModule.class.getName(), new Version(1, 0, 0, null, null, null));
    }

    @Override
    public void configurePolymorphicTypeValidator(BasicPolymorphicTypeValidator.Builder builder) {
        builder.allowIfSubType(PublicKeyAuthentication.class);
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixIn(PublicKeyAuthentication.class, PublicKeyAuthenticationMixin.class);
    }
}
