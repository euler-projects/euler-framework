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
 * representing an {@code POST /otp/tickets} request and its outcome.
 * <p>
 * In its <em>unauthenticated</em> form it carries the request parameters
 * extracted by {@code OtpTicketIssueAuthenticationConverter}. After
 * {@link OtpTicketIssueAuthenticationProvider} successfully issues a ticket,
 * the authenticated form additionally exposes the {@link OtpIssueResult} so
 * the endpoint filter can write the response body.
 */
public class OtpTicketIssueAuthenticationToken extends AbstractAuthenticationToken {

    private final String channel;
    private final String recipient;
    private final String identityId;
    private final String purpose;
    private final String codeChallenge;
    private final String codeChallengeMethod;
    private final OtpIssueResult issueResult;

    private OtpTicketIssueAuthenticationToken(String channel, String recipient, String identityId,
                                              String purpose, String codeChallenge, String codeChallengeMethod) {
        super(Collections.emptyList());
        this.channel = channel;
        this.recipient = recipient;
        this.identityId = identityId;
        this.purpose = purpose;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.issueResult = null;
        setAuthenticated(false);
    }

    private OtpTicketIssueAuthenticationToken(String channel, String recipient, String identityId,
                                              String purpose, String codeChallenge, String codeChallengeMethod,
                                              OtpIssueResult issueResult,
                                              Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.channel = channel;
        this.recipient = recipient;
        this.identityId = identityId;
        this.purpose = purpose;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.issueResult = issueResult;
        super.setAuthenticated(true);
    }

    /**
     * Create an unauthenticated token from converter-extracted parameters.
     */
    public static OtpTicketIssueAuthenticationToken unauthenticated(
            String channel, String recipient, String identityId,
            String purpose, String codeChallenge, String codeChallengeMethod) {
        return new OtpTicketIssueAuthenticationToken(channel, recipient, identityId,
                purpose, codeChallenge, codeChallengeMethod);
    }

    /**
     * Create an authenticated token wrapping the issued ticket's result.
     */
    public static OtpTicketIssueAuthenticationToken authenticated(
            String channel, String recipient, String identityId,
            String purpose, String codeChallenge, String codeChallengeMethod,
            OtpIssueResult issueResult,
            Collection<? extends GrantedAuthority> authorities) {
        return new OtpTicketIssueAuthenticationToken(channel, recipient, identityId,
                purpose, codeChallenge, codeChallengeMethod, issueResult, authorities);
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

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public OtpIssueResult getIssueResult() {
        return issueResult;
    }
}
