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
package org.eulerframework.security.oauth2.server.authorization.authentication;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

/**
 * The errors this grant answers with, shared by the grant provider, the assertion context and
 * the issuer authenticators so that all of them report a bad assertion in the same shape.
 * <p>
 * Package-private on purpose: these are this grant's protocol vocabulary rather than an API.
 * An issuer authenticator contributed from outside this package builds the same error itself
 * &mdash; {@code invalid_grant} with {@link #ERROR_URI} as its description of where the rule
 * comes from.
 */
final class JwtBearerErrors {

    /**
     * RFC 7523 Section 3.1, which is what makes every failure of the assertion itself an
     * {@code invalid_grant} rather than an {@code invalid_request} or a client error.
     */
    static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc7523#section-3.1";

    /**
     * The whole of what a caller is told when a refusal could otherwise say something about an
     * account: that it exists, that it is bound to something stronger than a key, that it does or
     * does not hold the key named, or that one could have been opened for it and was not.
     * <p>
     * These are the refusals that have to run before the signature is checked, because the key that
     * would verify it is the very thing being looked for. A caller who holds no key at all can
     * therefore reach every one of them, and any difference between their answers is a difference
     * it can read. The precise reason belongs in the server log, where an operator can act on it.
     */
    static final String NO_ACCOUNT_DETAIL = "the assertion does not authenticate";

    private JwtBearerErrors() {
    }

    /**
     * Refuse an assertion without saying anything about the account behind it.
     *
     * @see #NO_ACCOUNT_DETAIL
     */
    static OAuth2AuthenticationException refuse() {
        return invalidGrant(NO_ACCOUNT_DETAIL);
    }

    /**
     * The one error an assertion can cause. Descriptions say what was wrong with the assertion
     * and never whose account it was for, so the endpoint cannot be used to probe which
     * subjects exist.
     */
    static OAuth2AuthenticationException invalidGrant(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, description, ERROR_URI));
    }

    /**
     * A fault of this server rather than of the request: a token generator that produced
     * nothing, a missing authorization server context, an issuer authenticator that broke its
     * contract. Reported as {@code server_error} because blaming the client for it would send
     * it looking for a fix where there is none.
     */
    static OAuth2AuthenticationException serverError(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, description, ERROR_URI));
    }
}
