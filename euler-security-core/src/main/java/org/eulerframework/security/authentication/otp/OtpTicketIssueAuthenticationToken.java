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

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * {@link org.springframework.security.core.Authentication Authentication} token
 * for an OTP ticket issue request.
 * <p>
 * The unauthenticated form carries the extracted request parameters; the
 * authenticated form additionally exposes the {@link OtpIssueResult} used to
 * build the response body.
 */
public class OtpTicketIssueAuthenticationToken extends AbstractAuthenticationToken {

    private final String channel;
    private final String recipient;
    private final String identityId;
    private final String purpose;
    private final OtpIssueResult issueResult;

    private OtpTicketIssueAuthenticationToken(String channel, String recipient, String identityId,
                                              String purpose) {
        super(Collections.emptyList());
        this.channel = channel;
        this.recipient = recipient;
        this.identityId = identityId;
        this.purpose = purpose;
        this.issueResult = null;
        setAuthenticated(false);
    }

    private OtpTicketIssueAuthenticationToken(String channel, String recipient, String identityId,
                                              String purpose,
                                              OtpIssueResult issueResult,
                                              Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.channel = channel;
        this.recipient = recipient;
        this.identityId = identityId;
        this.purpose = purpose;
        this.issueResult = issueResult;
        super.setAuthenticated(true);
    }

    /**
     * Create an unauthenticated token from converter-extracted parameters.
     */
    public static OtpTicketIssueAuthenticationToken unauthenticated(
            String channel, String recipient, String identityId,
            String purpose) {
        return new OtpTicketIssueAuthenticationToken(channel, recipient, identityId, purpose);
    }

    /**
     * Create an authenticated token wrapping the issued ticket's result.
     */
    public static OtpTicketIssueAuthenticationToken authenticated(
            String channel, String recipient, String identityId,
            String purpose,
            OtpIssueResult issueResult,
            Collection<? extends GrantedAuthority> authorities) {
        return new OtpTicketIssueAuthenticationToken(channel, recipient, identityId,
                purpose, issueResult, authorities);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        // The OTP issue endpoint is anonymous - there is no principal.
        return this.recipient != null ? this.recipient : this.identityId;
    }

    public String getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getIdentityId() {
        return identityId;
    }

    public String getPurpose() {
        return purpose;
    }

    public OtpIssueResult getIssueResult() {
        return issueResult;
    }
}
