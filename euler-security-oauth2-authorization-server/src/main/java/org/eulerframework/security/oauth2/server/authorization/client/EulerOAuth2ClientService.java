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
     * Updates an existing client using full-overwrite (replace) semantics: the
     * submitted payload fully overwrites mutable client metadata; fields
     * omitted from {@code client} are cleared rather than preserved. Mirrors
     * HTTP {@code PUT}. Use {@link #patchClient(EulerOAuth2Client)} for
     * partial updates.
     * <p>
     * {@code clientSecret} rotation is not handled by this method &mdash; use
     * {@link #updateClientSecret(String, String)}; the secret column is left
     * untouched when the incoming value is {@code null}.
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
     * overwrite any previously persisted state. This is the repository-bridge
     * counterpart to {@link #updateClient(EulerOAuth2Client)}.
     * <p>
     * Three fields are treated as non-overwritable by {@code null}:
     * {@code clientIdIssuedAt} is a lifecycle audit field so the original
     * registration timestamp is not wiped by a partially populated payload;
     * {@code clientSecret} and {@code clientSecretExpiresAt} are credential
     * state owned exclusively by the rotation pipeline (see
     * {@link #updateClientSecret(String, String)}), so bulk replace paths
     * deliberately do not touch the credential.
     *
     * @param registeredClient the {@link RegisteredClient} instance carrying the updated state
     */
    void updateClient(RegisteredClient registeredClient);

    /**
     * Updates an existing client using patch semantics: only fields carrying a
     * non-{@code null} value on {@code client} are applied; {@code null}
     * fields leave the persisted state unchanged. Mirrors HTTP {@code PATCH}.
     * <p>
     * Fails when no client with the given
     * {@link EulerOAuth2Client#getRegistrationId() registrationId} exists.
     *
     * @param client the client carrying the fields to patch
     */
    void patchClient(EulerOAuth2Client client);

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
