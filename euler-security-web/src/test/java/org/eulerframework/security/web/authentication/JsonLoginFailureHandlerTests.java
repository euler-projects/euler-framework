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
package org.eulerframework.security.web.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies the exception-to-(status, error) mapping behind the JSON login
 * failure envelope. {@link JsonLoginFailureHandler#resolve} is a pure
 * function, so this needs no servlet collaborators.
 */
class JsonLoginFailureHandlerTests {

    @Test
    void badCredentialsMapsToTheGeneric401() {
        // Credential failures carry no dedicated code: the envelope must not
        // reveal anything beyond "authentication failed".
        assertMapping(new BadCredentialsException("Bad credentials"),
                HttpStatus.UNAUTHORIZED, "authentication_failed");
    }

    @Test
    void userNotFoundMapsToTheGeneric401() {
        // Spring hides user-not-found as bad credentials; both must read the
        // same to an unauthenticated client.
        assertMapping(new UsernameNotFoundException("no such user"),
                HttpStatus.UNAUTHORIZED, "authentication_failed");
    }

    @Test
    void lockedMapsTo403AccountLocked() {
        assertMapping(new LockedException("locked"),
                HttpStatus.FORBIDDEN, "account_locked");
    }

    @Test
    void disabledMapsTo403AccountDisabled() {
        assertMapping(new DisabledException("disabled"),
                HttpStatus.FORBIDDEN, "account_disabled");
    }

    @Test
    void accountExpiredMapsTo403AccountExpired() {
        assertMapping(new AccountExpiredException("expired"),
                HttpStatus.FORBIDDEN, "account_expired");
    }

    @Test
    void credentialsExpiredMapsToTheGeneric401() {
        // Expired credentials are a credential failure, not an account
        // status: blurred like the rest of them.
        assertMapping(new CredentialsExpiredException("expired"),
                HttpStatus.UNAUTHORIZED, "authentication_failed");
    }

    @Test
    void authenticationServiceExceptionMapsTo500ServerError() {
        assertMapping(new AuthenticationServiceException("backend down"),
                HttpStatus.INTERNAL_SERVER_ERROR, "server_error");
    }

    @Test
    void internalAuthenticationServiceExceptionMapsTo500ServerError() {
        // Subtype of AuthenticationServiceException; must still be a 500 so a
        // client does not read a server fault as bad credentials.
        assertMapping(new InternalAuthenticationServiceException("backend down"),
                HttpStatus.INTERNAL_SERVER_ERROR, "server_error");
    }

    @Test
    void providerNotFoundMapsTo500ServerError() {
        assertMapping(new ProviderNotFoundException("no provider"),
                HttpStatus.INTERNAL_SERVER_ERROR, "server_error");
    }

    @Test
    void unknownAuthenticationExceptionFallsBackTo401() {
        assertMapping(new AuthenticationException("something else") {
                },
                HttpStatus.UNAUTHORIZED, "authentication_failed");
    }

    private static void assertMapping(AuthenticationException exception, HttpStatus status, String error) {
        JsonLoginFailureHandler.Failure failure = JsonLoginFailureHandler.resolve(exception);
        assertEquals(status, failure.status(), "status for " + exception.getClass().getSimpleName());
        assertEquals(error, failure.error(), "error for " + exception.getClass().getSimpleName());
    }
}
