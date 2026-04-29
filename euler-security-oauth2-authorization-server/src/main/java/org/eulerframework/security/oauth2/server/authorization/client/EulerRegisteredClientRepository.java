package org.eulerframework.security.oauth2.server.authorization.client;

import org.eulerframework.security.oauth2.server.authorization.util.OAuth2ClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * A {@link RegisteredClientRepository} implementation that delegates to
 * {@link EulerOAuth2ClientService} for client storage and retrieval, and uses
 * {@link OAuth2ClientUtils} for converting to {@link RegisteredClient}.
 */
public class EulerRegisteredClientRepository implements RegisteredClientRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EulerOAuth2ClientService eulerOAuth2ClientService;

    public EulerRegisteredClientRepository(EulerOAuth2ClientService eulerOAuth2ClientService) {
        this.eulerOAuth2ClientService = eulerOAuth2ClientService;
    }

    public EulerRegisteredClientRepository(EulerOAuth2ClientService eulerOAuth2ClientService, Collection<RegisteredClient> registeredClients) {
        this.eulerOAuth2ClientService = eulerOAuth2ClientService;
        if (!CollectionUtils.isEmpty(registeredClients)) {
            for (RegisteredClient registeredClient : registeredClients) {
                this.save(registeredClient);
            }
        }
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Assert.notNull(registeredClient, "RegisteredClient must not be null");

        // RegisteredClient.Builder guarantees a non-null id, so a single lookup
        // by registrationId is sufficient to decide between insert and update.
        EulerOAuth2Client exists = this.eulerOAuth2ClientService.loadClientByRegistrationId(registeredClient.getId());
        if (exists == null) {
            this.eulerOAuth2ClientService.createClient(registeredClient);
        } else {
            this.eulerOAuth2ClientService.updateClient(registeredClient);
        }
    }

    @Override
    public RegisteredClient findById(String id) {
        EulerOAuth2Client client = this.eulerOAuth2ClientService.loadClientByRegistrationId(id);
        return OAuth2ClientUtils.toRegisteredClient(client);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        EulerOAuth2Client client = this.eulerOAuth2ClientService.loadClientByClientId(clientId);
        return OAuth2ClientUtils.toRegisteredClient(client);
    }
}
