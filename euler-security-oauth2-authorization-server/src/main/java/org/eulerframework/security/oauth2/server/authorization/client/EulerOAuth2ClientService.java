package org.eulerframework.security.oauth2.server.authorization.client;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.List;

/**
 * Service interface for managing OAuth 2.0 clients.
 */
public interface EulerOAuth2ClientService {

    /**
     * Creates a new OAuth 2.0 client from a {@link RegisteredClient} instance.
     *
     * @param registeredClient the {@link RegisteredClient} instance
     * @return the created client
     */
    EulerOAuth2Client createClient(RegisteredClient registeredClient);

    /**
     * Creates a new OAuth 2.0 client.
     *
     * @param client the client to create
     * @return the created client
     */
    EulerOAuth2Client createClient(EulerOAuth2Client client);

    /**
     * Loads a client by its client ID.
     *
     * @param clientId the client identifier
     * @return the client, or {@code null} if not found
     */
    EulerOAuth2Client loadClientByClientId(String clientId);

    /**
     * Loads a client by its registration ID.
     *
     * @param registrationId the registration identifier
     * @return the client, or {@code null} if not found
     */
    EulerOAuth2Client loadClientByRegistrationId(String registrationId);

    /**
     * Lists clients with pagination.
     *
     * @param offset the offset of the first result
     * @param limit  the maximum number of results
     * @return a list of clients
     */
    List<EulerOAuth2Client> listClients(int offset, int limit);

    /**
     * Updates an existing client using patch semantics.
     * <p>
     * Non-boolean properties are only updated when the incoming value is non-null.
     * Boolean properties are always overwritten.
     *
     * @param client the client with updated fields
     */
    void updateClient(EulerOAuth2Client client);

    /**
     * Deletes a client by its registration ID.
     *
     * @param registrationId the registration identifier
     */
    void deleteClient(String registrationId);

    /**
     * Updates only the client secret for the specified client.
     *
     * @param registrationId the registration identifier
     * @param clientSecret   the new client secret (already encoded)
     */
    void updateClientSecret(String registrationId, String clientSecret);
}
