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

import org.eulerframework.resource.Tag;
import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.eulerframework.security.authentication.wechat.WechatAuthorizationCodeAuthenticationToken;
import org.eulerframework.security.core.EulerGrantedAuthority;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.jackson.SecurityJacksonModule;
import tools.jackson.core.Version;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Contributes the Euler types that the security {@code JsonMapper} may deserialize, and the mixins
 * telling it how.
 * <p>
 * Any {@code Authentication} a grant provider writes into an authorization's
 * {@code java.security.Principal} attribute must be registered here, together with every
 * non-final type it holds. The authorization store reads the attribute map back in one pass, so
 * one unregistered type fails the read outright rather than degrading a single field.
 */
public class EulerSecurityJacksonModule extends SecurityJacksonModule {

    public EulerSecurityJacksonModule() {
        super(EulerSecurityJacksonModule.class.getName(), new Version(1, 0, 0, null, null, null));
    }

    @Override
    public void configurePolymorphicTypeValidator(BasicPolymorphicTypeValidator.Builder builder) {
        builder.allowIfSubType(EulerUserDetails.class)
                .allowIfSubType(EulerGrantedAuthority.class)
                .allowIfSubType(WechatAuthorizationCodeAuthenticationToken.class)
                .allowIfSubType(OneTimePasswordAuthenticationToken.class)
                .allowIfSubType(UserIdentity.class)
                .allowIfSubType(Tag.class)
                .allowIfSubType("java.util.ImmutableCollections$List12")
                .allowIfSubType("java.util.ImmutableCollections$ListN")
                .allowIfSubType("java.util.ImmutableCollections$Set12")
                .allowIfSubType("java.util.ImmutableCollections$SetN")
                .allowIfSubType("java.util.ImmutableCollections$Map1")
                .allowIfSubType("java.util.ImmutableCollections$MapN");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.setMixIn(EulerUserDetails.class, EulerUserDetailsMixin.class);
        context.setMixIn(EulerGrantedAuthority.class, EulerGrantedAuthorityMixin.class);
        context.setMixIn(WechatAuthorizationCodeAuthenticationToken.class,
                WechatAuthorizationCodeAuthenticationTokenMixin.class);
        context.setMixIn(OneTimePasswordAuthenticationToken.class, OneTimePasswordAuthenticationTokenMixin.class);
        context.setMixIn(UserIdentity.class, UserIdentityMixin.class);
    }
}
