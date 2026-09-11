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

import org.eulerframework.security.authentication.appattest.AppAttestUtils;
import org.eulerframework.security.authentication.appattest.RegisteredApp;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerConfigurationSettingNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AppAttestOAuth2ClientProvisioningListener}, focusing on the
 * STATIC baseline provisioning, the DYNAMIC marker, and the provision-if-absent
 * guard that must never overwrite an administrator's customization.
 */
class AppAttestOAuth2ClientProvisioningListenerTest {

    private static RegisteredApp app(RegisteredApp.OAuth2ClientType type, boolean oauth2Enabled) {
        RegisteredApp.Builder builder = RegisteredApp.withId("myapp")
                .teamId("ABCD1234EF")
                .bundleId("com.example.app")
                .oauth2Enabled(oauth2Enabled);
        if (oauth2Enabled) {
            builder.oauth2ClientType(type);
        }
        return builder.build();
    }

    @Test
    void staticAppProvisionsBaselineClientWithStaticMarker() {
        FakeRegisteredClientRepository repo = new FakeRegisteredClientRepository();
        AppAttestOAuth2ClientProvisioningListener listener = new AppAttestOAuth2ClientProvisioningListener(repo);
        RegisteredApp app = app(RegisteredApp.OAuth2ClientType.STATIC, true);

        listener.onRegisteredAppSaved(app);

        RegisteredClient client = repo.findByClientId(AppAttestUtils.staticClientId(app));
        assertNotNull(client, "STATIC app should be provisioned with its deterministic client_id");
        assertTrue(client.getClientAuthenticationMethods()
                .contains(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH));
        assertTrue(client.getAuthorizationGrantTypes().contains(EulerAuthorizationGrantType.APP_ASSERTION));
        assertEquals(RegisteredApp.OAuth2ClientType.STATIC.name(),
                client.getClientSettings().getSetting(EulerConfigurationSettingNames.Client.APP_ATTEST_CLIENT_TYPE));
    }

    @Test
    void provisionIfAbsentDoesNotOverwriteAdminCustomization() {
        FakeRegisteredClientRepository repo = new FakeRegisteredClientRepository();
        AppAttestOAuth2ClientProvisioningListener listener = new AppAttestOAuth2ClientProvisioningListener(repo);
        RegisteredApp app = app(RegisteredApp.OAuth2ClientType.STATIC, true);

        listener.onRegisteredAppSaved(app);
        RegisteredClient provisioned = repo.findByClientId(AppAttestUtils.staticClientId(app));
        assertNotNull(provisioned);

        // An administrator customizes the client (adds a scope), keeping id + client_id.
        RegisteredClient customized = RegisteredClient.from(provisioned).scope("admin-added").build();
        repo.save(customized);

        // The listener fires again (e.g. on every restart preload): it must NOT overwrite.
        listener.onRegisteredAppSaved(app);

        RegisteredClient after = repo.findByClientId(AppAttestUtils.staticClientId(app));
        assertNotNull(after);
        assertTrue(after.getScopes().contains("admin-added"),
                "administrator customization must be preserved by provision-if-absent");
    }

    @Test
    void dynamicAppIsNotProvisioned() {
        FakeRegisteredClientRepository repo = new FakeRegisteredClientRepository();
        AppAttestOAuth2ClientProvisioningListener listener = new AppAttestOAuth2ClientProvisioningListener(repo);

        listener.onRegisteredAppSaved(app(RegisteredApp.OAuth2ClientType.DYNAMIC, true));

        assertNull(repo.findByClientId(AppAttestUtils.staticClientId(app(RegisteredApp.OAuth2ClientType.DYNAMIC, true))));
        assertTrue(repo.byId.isEmpty(), "DYNAMIC clients are provisioned per-key at registration time, not here");
    }

    @Test
    void oauth2DisabledAppIsNotProvisioned() {
        FakeRegisteredClientRepository repo = new FakeRegisteredClientRepository();
        AppAttestOAuth2ClientProvisioningListener listener = new AppAttestOAuth2ClientProvisioningListener(repo);

        listener.onRegisteredAppSaved(app(null, false));

        assertTrue(repo.byId.isEmpty());
    }

    /**
     * Minimal {@link RegisteredClientRepository} mirroring the upsert-by-id semantics of
     * {@code EulerRegisteredClientRepository.save}.
     */
    static class FakeRegisteredClientRepository implements RegisteredClientRepository {

        final Map<String, RegisteredClient> byId = new LinkedHashMap<>();

        @Override
        public void save(RegisteredClient registeredClient) {
            this.byId.put(registeredClient.getId(), registeredClient);
        }

        @Override
        public RegisteredClient findById(String id) {
            return this.byId.get(id);
        }

        @Override
        public RegisteredClient findByClientId(String clientId) {
            return this.byId.values().stream()
                    .filter(c -> c.getClientId().equals(clientId))
                    .findFirst()
                    .orElse(null);
        }
    }
}
