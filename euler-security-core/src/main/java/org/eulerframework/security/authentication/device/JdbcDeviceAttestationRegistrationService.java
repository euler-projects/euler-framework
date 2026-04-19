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

package org.eulerframework.security.authentication.device;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.Assert;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * JDBC implementation of {@link DeviceAttestationRegistrationService} that persists
 * device attestation registrations in a relational database.
 * <p>
 * By default, this implementation uses the table {@code device_attestation_registration}
 * with the following schema:
 * <pre>{@code
 * CREATE TABLE device_attestation_registration (
 *     key_id                        VARCHAR(255)  NOT NULL PRIMARY KEY,
 *     team_id                       VARCHAR(255)  NOT NULL,
 *     bundle_id                     VARCHAR(255)  NOT NULL,
 *     client_id                     VARCHAR(255)  NULL,
 *     aaguid                        BLOB          NOT NULL,
 *     credential_id                 BLOB          NOT NULL,
 *     attestation_certificate_chain BLOB          NOT NULL,
 *     receipt                       BLOB          NOT NULL,
 *     public_key                    BLOB          NOT NULL,
 *     jwks                          TEXT          NOT NULL,
 *     sign_count                    BIGINT        NOT NULL
 * );
 * }</pre>
 *
 * @see DeviceAttestationRegistrationService
 * @see InMemoryDeviceAttestationRegistrationService
 */
public class JdbcDeviceAttestationRegistrationService implements DeviceAttestationRegistrationService {

    // @formatter:off
    private static final String DEFAULT_TABLE_NAME = "device_attestation_registration";

    private static final String COLUMN_KEY_ID                        = "key_id";
    private static final String COLUMN_TEAM_ID                       = "team_id";
    private static final String COLUMN_BUNDLE_ID                     = "bundle_id";
    private static final String COLUMN_CLIENT_ID                     = "client_id";
    private static final String COLUMN_AAGUID                        = "aaguid";
    private static final String COLUMN_CREDENTIAL_ID                 = "credential_id";
    private static final String COLUMN_ATTESTATION_CERTIFICATE_CHAIN = "attestation_certificate_chain";
    private static final String COLUMN_RECEIPT                       = "receipt";
    private static final String COLUMN_PUBLIC_KEY                    = "public_key";
    private static final String COLUMN_JWKS                          = "jwks";
    private static final String COLUMN_SIGN_COUNT                    = "sign_count";
    // @formatter:on

    private static final String INSERT_REGISTRATION_SQL =
            "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_REGISTRATION_SQL =
            "SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s FROM %s WHERE %s = ?";

    private static final String UPDATE_SIGN_COUNT_SQL =
            "UPDATE %s SET %s = ? WHERE %s = ? AND %s < ?";

    private final JdbcOperations jdbcOperations;
    private final String insertSql;
    private final String selectSql;
    private final String updateSignCountSql;

    /**
     * Create a new {@code JdbcDeviceAttestRegistrationService} with the default table name.
     *
     * @param jdbcOperations the JDBC operations (must not be {@code null})
     */
    public JdbcDeviceAttestationRegistrationService(JdbcOperations jdbcOperations) {
        this(jdbcOperations, DEFAULT_TABLE_NAME);
    }

    /**
     * Create a new {@code JdbcDeviceAttestRegistrationService} with a custom table name.
     *
     * @param jdbcOperations the JDBC operations (must not be {@code null})
     * @param tableName      the table name to use (must not be empty)
     */
    public JdbcDeviceAttestationRegistrationService(JdbcOperations jdbcOperations, String tableName) {
        Assert.notNull(jdbcOperations, "jdbcOperations must not be null");
        Assert.hasText(tableName, "tableName must not be empty");
        this.jdbcOperations = jdbcOperations;
        this.insertSql = String.format(INSERT_REGISTRATION_SQL, tableName,
                COLUMN_KEY_ID, COLUMN_TEAM_ID, COLUMN_BUNDLE_ID, COLUMN_CLIENT_ID,
                COLUMN_AAGUID, COLUMN_CREDENTIAL_ID,
                COLUMN_ATTESTATION_CERTIFICATE_CHAIN, COLUMN_RECEIPT,
                COLUMN_PUBLIC_KEY, COLUMN_JWKS, COLUMN_SIGN_COUNT);
        this.selectSql = String.format(SELECT_REGISTRATION_SQL,
                COLUMN_KEY_ID, COLUMN_TEAM_ID, COLUMN_BUNDLE_ID, COLUMN_CLIENT_ID,
                COLUMN_AAGUID, COLUMN_CREDENTIAL_ID,
                COLUMN_ATTESTATION_CERTIFICATE_CHAIN, COLUMN_RECEIPT,
                COLUMN_PUBLIC_KEY, COLUMN_JWKS, COLUMN_SIGN_COUNT,
                tableName, COLUMN_KEY_ID);
        this.updateSignCountSql = String.format(UPDATE_SIGN_COUNT_SQL, tableName,
                COLUMN_SIGN_COUNT, COLUMN_KEY_ID, COLUMN_SIGN_COUNT);
    }

    @Override
    public void saveRegistration(DeviceAttestationRegistration registration) {
        Assert.notNull(registration, "registration must not be null");
        Assert.hasText(registration.getKeyId(), "keyId must not be empty");
        this.jdbcOperations.update(this.insertSql, ps -> {
            int index = 0;
            ps.setString(++index, registration.getKeyId());
            ps.setString(++index, registration.getTeamId());
            ps.setString(++index, registration.getBundleId());
            ps.setString(++index, registration.getClientId());
            ps.setBytes(++index, registration.getAaguid());
            ps.setBytes(++index, registration.getCredentialId());
            ps.setBytes(++index, registration.getAttestationCertificateChain());
            ps.setBytes(++index, registration.getReceipt());
            ps.setBytes(++index, registration.getPublicKey().getEncoded());
            ps.setString(++index, registration.getJwks());
            ps.setLong(++index, registration.getSignCount());
        });
    }

    @Override
    public DeviceAttestationRegistration findByKeyId(String keyId) {
        return this.jdbcOperations.query(this.selectSql,
                ps -> ps.setString(1, keyId),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    PublicKey publicKey = deserializePublicKey(rs.getBytes(COLUMN_PUBLIC_KEY));
                    return new DeviceAttestationRegistration(
                            rs.getString(COLUMN_KEY_ID),
                            rs.getString(COLUMN_TEAM_ID),
                            rs.getString(COLUMN_BUNDLE_ID),
                            rs.getString(COLUMN_CLIENT_ID),
                            rs.getBytes(COLUMN_AAGUID),
                            rs.getBytes(COLUMN_CREDENTIAL_ID),
                            rs.getBytes(COLUMN_ATTESTATION_CERTIFICATE_CHAIN),
                            rs.getBytes(COLUMN_RECEIPT),
                            publicKey,
                            rs.getString(COLUMN_JWKS),
                            rs.getLong(COLUMN_SIGN_COUNT)
                    );
                });
    }

    @Override
    public void updateSignCount(String keyId, long newSignCount) {
        this.jdbcOperations.update(this.updateSignCountSql, newSignCount, keyId, newSignCount);
    }

    /**
     * Deserialize a public key from its X.509 encoded form.
     * <p>
     * Device attestation typically uses EC keys on the P-256 curve.
     */
    private static PublicKey deserializePublicKey(byte[] encoded) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize EC public key", e);
        }
    }
}
