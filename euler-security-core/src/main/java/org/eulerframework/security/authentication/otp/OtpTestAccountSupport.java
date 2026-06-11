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
package org.eulerframework.security.authentication.otp;

import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Whitelist of test recipients for which the OTP issue flow short-circuits to
 * a fixed OTP value, skips the real {@link OtpChannel} delivery, and logs a
 * single {@code WARN} line.
 * <p>
 * Verification needs no special handling: the ticket persisted by
 * {@link OtpTicketIssueAuthenticationProvider} already carries the fixed OTP
 * value, so {@link OtpTicketService#consume(String, String, String, String)}
 * matches it via the standard plaintext compare path.
 * <p>
 * This support is intentionally agnostic to recipient format - phone numbers,
 * email addresses or any other channel-addressable string may be listed; the
 * match is a simple set membership check on the resolved recipient
 * (post {@link OtpRecipientResolver} resolution when {@code identity_id} is
 * used).
 *
 * @see OtpTicketIssueAuthenticationProvider
 */
public final class OtpTestAccountSupport {

    private static final Pattern NUMERIC = Pattern.compile("\\d+");

    private final Set<String> accounts;
    private final String fixedOtp;

    /**
     * @param accounts test recipients (phone numbers, emails, ...); must not
     *                 be {@code null} or empty
     * @param fixedOtp the OTP value handed out (and accepted on verification)
     *                 for any test recipient; must be a non-empty numeric
     *                 string
     */
    public OtpTestAccountSupport(Collection<String> accounts, String fixedOtp) {
        Assert.notEmpty(accounts, "accounts must not be empty");
        Assert.hasText(fixedOtp, "fixedOtp must not be empty");
        Assert.isTrue(NUMERIC.matcher(fixedOtp).matches(),
                "fixedOtp must contain digits only");
        Set<String> copy = new LinkedHashSet<>(accounts.size());
        for (String account : accounts) {
            Assert.hasText(account, "test account entry must not be empty");
            copy.add(account);
        }
        this.accounts = Collections.unmodifiableSet(copy);
        this.fixedOtp = fixedOtp;
    }

    /**
     * @return {@code true} if {@code recipient} matches one of the configured
     *         test accounts (case-sensitive exact match)
     */
    public boolean isTestAccount(String recipient) {
        return recipient != null && this.accounts.contains(recipient);
    }

    /**
     * @return the fixed OTP value handed out for test recipients
     */
    public String getFixedOtp() {
        return this.fixedOtp;
    }

    /**
     * @return an unmodifiable view of the configured test recipients
     */
    public Set<String> getAccounts() {
        return this.accounts;
    }
}
