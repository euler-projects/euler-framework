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

package org.eulerframework.security.oauth2.server.authorization.client;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;

import org.eulerframework.security.authentication.appattest.AppAttestUtils;
import org.eulerframework.security.authentication.appattest.RegisteredApp;
import org.eulerframework.security.authentication.appattest.RegisteredAppChangeListener;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerConfigurationSettingNames;

/**
 * A {@link RegisteredAppChangeListener} that provisions an OAuth2 {@link RegisteredClient}
 * for each {@link RegisteredApp} with {@link RegisteredApp.OAuth2ClientType#STATIC STATIC} client type.
 * <p>
 * This listener implements <b>provision-if-absent</b> semantics: when a registered app is
 * saved, it checks whether a corresponding OAuth2 client already exists (by {@code client_id}).
 * If the client exists, no action is taken—this preserves any modifications made by an
 * administrator through the client management UI.
 * <p>
 * The generated {@code client_id} is the base64url-encoded SHA-256 hash of the app ID,
 * which deterministically maps each app to a single OAuth2 client identity.
 *
 * @see AppAttestUtils#staticClientId(org.eulerframework.security.authentication.appattest.RegisteredApp)
 * @see RegisteredAppChangeListener
 */
public class AppAttestOAuth2ClientProvisioningListener implements RegisteredAppChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(AppAttestOAuth2ClientProvisioningListener.class);

    private final RegisteredClientRepository registeredClientRepository;

    public AppAttestOAuth2ClientProvisioningListener(RegisteredClientRepository registeredClientRepository) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository must not be null");
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public void onRegisteredAppSaved(RegisteredApp app) {
        if (!app.isOauth2Enabled() || app.getOauth2ClientType() != RegisteredApp.OAuth2ClientType.STATIC) {
            return;
        }

        String clientId = AppAttestUtils.staticClientId(app);

        // Provision-if-absent: never overwrite an existing client. This listener fires on
        // every app save (including startup preload), and RegisteredClientRepository.save
        // performs a full replace when the registrationId already exists; without this
        // guard each restart would reset administrator customizations back to the baseline
        // below. The client_id is deterministic, so an administrator-customized client
        // (which keeps its id and client_id) is always found here and skipped.
        if (this.registeredClientRepository.findByClientId(clientId) != null) {
            logger.debug("OAuth2 client already exists for app '{}' (client_id '{}'), skipping provisioning",
                    app.getAppId(), clientId);
            return;
        }

        String registrationId = UUID.nameUUIDFromBytes(
                ("app-attest:" + app.getAppId()).getBytes(StandardCharsets.UTF_8)).toString();

        // The app_assertion grant is deprecated but is still provisioned here: released STATIC
        // clients renew their tokens with it. See EulerAuthorizationGrantType#APP_ASSERTION.
        RegisteredClient registeredClient = RegisteredClient.withId(registrationId)
                .clientId(clientId)
                .clientName(app.getAppId())
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH)
                .authorizationGrantType(EulerAuthorizationGrantType.APP_ASSERTION)
                .scope(OidcScopes.OPENID)
                .clientSettings(ClientSettings.builder()
                        .setting(EulerConfigurationSettingNames.Client.APP_ATTEST_CLIENT_TYPE,
                                RegisteredApp.OAuth2ClientType.STATIC.name())
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofDays(7))
                        .build())
                .build();

        this.registeredClientRepository.save(registeredClient);
        logger.info("Provisioned OAuth2 client for app '{}' with client_id '{}'", app.getAppId(), clientId);
    }
}
