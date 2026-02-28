package org.eulerframework.data.file.web.security;

import java.time.Instant;

public interface FileToken {
    /**
     * Returns the file ID for which the token was issued.
     */
    String getFileId();

    /**
     * Returns the token value.
     *
     * @return the token value
     */
    String getTokenValue();

    /**
     * Returns the time at which the token was issued.
     */
    Instant getIssuedAt();

    /**
     * Returns the expiration time on or after which the token MUST NOT be accepted.
     */
    Instant getExpiresAt();
}
