/*
 * Copyright 2013-2026 the original author or authors.
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
package org.eulerframework.security.authentication.apple;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.Assert;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC-backed implementation of {@link AppleAppAttestKeyCredentialService}.
 * <p>
 * Requires the following table:
 * <pre>
 * CREATE TABLE app_attest_key_credential (
 *     key_id      VARCHAR(256) PRIMARY KEY,
 *     public_key  BLOB         NOT NULL,
 *     sign_count  BIGINT       NOT NULL DEFAULT 0
 * );
 * </pre>
 */
public class JdbcAppleAppAttestKeyCredentialService implements AppleAppAttestKeyCredentialService {

    private static final String DEFAULT_TABLE_NAME = "app_attest_key_credential";
    private static final java.util.regex.Pattern TABLE_NAME_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z0-9_]+$");

    private final JdbcOperations jdbcOperations;
    private String tableName = DEFAULT_TABLE_NAME;

    public JdbcAppleAppAttestKeyCredentialService(JdbcOperations jdbcOperations) {
        Assert.notNull(jdbcOperations, "jdbcOperations must not be null");
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public void saveKeyCredential(AppleAppAttestKeyCredential credential) {
        Assert.notNull(credential, "credential must not be null");
        this.jdbcOperations.update(
                "INSERT INTO " + this.tableName + " (key_id, public_key, sign_count) VALUES (?, ?, ?)",
                credential.getKeyId(),
                credential.getPublicKey().getEncoded(),
                credential.getSignCount()
        );
    }

    @Override
    public AppleAppAttestKeyCredential getKeyCredential(String keyId) {
        List<AppleAppAttestKeyCredential> results = this.jdbcOperations.query(
                "SELECT key_id, public_key, sign_count FROM " + this.tableName + " WHERE key_id = ?",
                this::mapRow,
                keyId
        );
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void updateSignCount(String keyId, long newSignCount) {
        this.jdbcOperations.update(
                "UPDATE " + this.tableName + " SET sign_count = ? WHERE key_id = ? AND sign_count < ?",
                newSignCount, keyId, newSignCount
        );
    }

    public void setTableName(String tableName) {
        Assert.hasText(tableName, "tableName must not be empty");
        if (!TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new IllegalArgumentException("tableName must only contain alphanumeric characters and underscores");
        }
        this.tableName = tableName;
    }

    private AppleAppAttestKeyCredential mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            String keyId = rs.getString("key_id");
            byte[] publicKeyBytes = rs.getBytes("public_key");
            long signCount = rs.getLong("sign_count");

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            return new AppleAppAttestKeyCredential(keyId, publicKey, signCount);
        } catch (Exception e) {
            throw new SQLException("Failed to deserialize public key", e);
        }
    }
}
