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
package org.eulerframework.security.web.login;

/**
 * A registered {@code oauth2} login method: sign-in delegated to an
 * external identity provider, served by the {@code oauth2} handler in
 * {@code euler-security-oauth2-client}.
 *
 * <p>Several registrations of this type may coexist, one per provider.
 */
public final class RegisteredOAuth2LoginMethod extends RegisteredLoginMethod {

    /** The {@code method-type} this registration is declared under. */
    public static final String TYPE = "oauth2";

    private final String provider;
    private final String oauthClientRegistrationId;

    /**
     * @param id                        the identifier this registration
     *                                  is stored under
     * @param name                      the name clients address this
     *                                  method by
     * @param identityType              the identity type this method
     *                                  establishes, e.g. {@code google}
     * @param primary                   whether the method belongs to the
     *                                  primary group
     * @param provider                  the provider federated to, or
     *                                  {@code null} to fall back on the
     *                                  identity type
     * @param oauthClientRegistrationId the {@code ClientRegistration} to
     *                                  use, or {@code null} to fall back
     *                                  on the provider
     */
    public RegisteredOAuth2LoginMethod(String id, String name, String identityType, boolean primary,
                                       String provider, String oauthClientRegistrationId) {
        super(TYPE, id, name, identityType, primary);
        this.provider = provider;
        this.oauthClientRegistrationId = oauthClientRegistrationId;
    }

    /**
     * The provider this method federates to, published to clients so
     * that they can brand the option. {@code null} leaves it to fall
     * back on the {@link #getIdentityType() identity type}.
     */
    public String getProvider() {
        return this.provider;
    }

    /**
     * The {@code registrationId} of the {@code ClientRegistration} that
     * carries the client credentials and endpoints, as declared under
     * {@code spring.security.oauth2.client.registration.*}. {@code null}
     * leaves it to fall back on the provider, which covers the common
     * case of one registration per provider.
     */
    public String getOauthClientRegistrationId() {
        return this.oauthClientRegistrationId;
    }
}
