package org.eulerframework.security.oauth2.server.authorization.client;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.List;

/**
 * Service interface for managing OAuth 2.0 clients.
 */
public interface EulerOAuth2ClientService {

    /**
     * Creates a new OAuth 2.0 client.
     * <p>
     * On this path the server fully owns the generated identifiers: any
     * {@code registrationId}, {@code clientId} or {@code clientIdIssuedAt}
     * carried by {@code client} is deliberately overwritten with server-minted
     * values.
     *
     * @param client the client to create
     * @return the created client
     */
    EulerOAuth2Client createClient(EulerOAuth2Client client);

    /**
     * Creates a new OAuth 2.0 client from a {@link RegisteredClient} instance.
     * <p>
     * {@code registeredClient} is assumed to be a fully-assembled registration:
     * {@link RegisteredClient#getId()} and {@link RegisteredClient#getClientId()}
     * must be provided by the caller and are persisted as-is. Only
     * {@link RegisteredClient#getClientIdIssuedAt()} is back-filled by the
     * server when left unset.
     *
     * @param registeredClient the {@link RegisteredClient} instance
     * @return the created client
     */
    EulerOAuth2Client createClient(RegisteredClient registeredClient);

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
     * Updates an existing client from a {@link RegisteredClient} instance using
     * full-overwrite semantics, mirroring the contract of
     * {@link org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository#save(RegisteredClient)
     * RegisteredClientRepository#save}.
     * <p>
     * The registration is located by {@link RegisteredClient#getId()} and every
     * mapped field is then replaced with the value carried by
     * {@code registeredClient} &mdash; including {@code null} values, which
     * overwrite any previously persisted state. This is the deliberate
     * counterpart to the patch semantics exposed by
     * {@link #updateClient(EulerOAuth2Client)}.
     * <p>
     * The sole exception is {@code clientIdIssuedAt}: it is a lifecycle audit
     * field and is preserved when the incoming {@code registeredClient} does
     * not carry a value, so the original registration timestamp is not wiped
     * by a partially populated payload.
     *
     * @param registeredClient the {@link RegisteredClient} instance carrying the updated state
     */
    void updateClient(RegisteredClient registeredClient);

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
