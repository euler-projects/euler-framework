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

package org.eulerframework.security.web.authentication.otp;

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.authentication.otp.OtpTicketIssueAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * Extracts {@code POST /otp/tickets} parameters and constructs an
 * {@link OtpTicketIssueAuthenticationToken}.
 * <p>
 * Validation rules:
 * <ul>
 *     <li>{@code channel} must be present.</li>
 *     <li>When PKCE is required (see
 *         {@code euler.security.otp.pkce.enabled}): {@code code_challenge}
 *         must be present and {@code code_challenge_method} must equal
 *         {@value #CODE_CHALLENGE_METHOD_S256} (PKCE only supports S256 in
 *         this module). When PKCE is disabled, both parameters are ignored
 *         and stored as {@code null} on the resulting ticket.</li>
 *     <li>Exactly one of {@code recipient} or {@code identity_id} must be supplied.
 *         Supplying both, or neither, is treated as a bad request.</li>
 * </ul>
 * Returns {@code null} when any rule above fails so the filter can emit a unified
 * {@code invalid_request} error response.
 *
 * @see OtpTicketIssueEndpointFilter
 */
public class OtpTicketIssueAuthenticationConverter implements AuthenticationConverter {

    static final String PARAM_CHANNEL = "channel";
    static final String PARAM_RECIPIENT = "recipient";
    static final String PARAM_IDENTITY_ID = "identity_id";
    static final String PARAM_PURPOSE = "purpose";
    static final String PARAM_CODE_CHALLENGE = "code_challenge";
    static final String PARAM_CODE_CHALLENGE_METHOD = "code_challenge_method";

    static final String CODE_CHALLENGE_METHOD_S256 = "S256";

    private final boolean pkceRequired;

    /**
     * Backwards-compatible constructor: PKCE required.
     */
    public OtpTicketIssueAuthenticationConverter() {
        this(true);
    }

    public OtpTicketIssueAuthenticationConverter(boolean pkceRequired) {
        this.pkceRequired = pkceRequired;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        String channel = request.getParameter(PARAM_CHANNEL);
        String recipient = request.getParameter(PARAM_RECIPIENT);
        String identityId = request.getParameter(PARAM_IDENTITY_ID);
        String purpose = request.getParameter(PARAM_PURPOSE);
        String codeChallenge = request.getParameter(PARAM_CODE_CHALLENGE);
        String codeChallengeMethod = request.getParameter(PARAM_CODE_CHALLENGE_METHOD);

        if (!StringUtils.hasText(channel)) {
            return null;
        }

        if (this.pkceRequired) {
            if (!StringUtils.hasText(codeChallenge)) {
                return null;
            }
            if (!CODE_CHALLENGE_METHOD_S256.equals(codeChallengeMethod)) {
                return null;
            }
        } else {
            // PKCE disabled: ignore client-supplied PKCE parameters
            // entirely so they cannot end up bound to the ticket.
            codeChallenge = null;
            codeChallengeMethod = null;
        }

        // recipient / identity_id are mutually exclusive but at least one is required
        boolean hasRecipient = StringUtils.hasText(recipient);
        boolean hasIdentityId = StringUtils.hasText(identityId);
        if (hasRecipient == hasIdentityId) {
            return null;
        }

        return OtpTicketIssueAuthenticationToken.unauthenticated(
                channel,
                hasRecipient ? recipient : null,
                hasIdentityId ? identityId : null,
                StringUtils.hasText(purpose) ? purpose : null,
                codeChallenge,
                codeChallengeMethod);
    }
}
