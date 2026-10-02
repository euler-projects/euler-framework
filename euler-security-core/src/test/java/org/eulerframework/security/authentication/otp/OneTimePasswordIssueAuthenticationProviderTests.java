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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class OneTimePasswordIssueAuthenticationProviderTests {

    private static final OneTimePasswordPolicy POLICY = new OneTimePasswordPolicy(
            6, Duration.ofMinutes(5), Duration.ofSeconds(60), 5);

    @Test
    void issueSucceedsEvenWhenAsyncDeliveryFails() {
        OneTimePasswordIssueAuthenticationProvider provider = createProvider(new SingleOneTimePasswordChannel() {
            @Override
            public CompletableFuture<Void> send(OneTimePasswordDelivering delivering) {
                return CompletableFuture.failedFuture(new OneTimePasswordDeliveryException("delivery failed"));
            }

            @Override
            public String getChannel() {
                return "sms";
            }
        });

        Authentication result = provider.authenticate(OneTimePasswordIssueAuthenticationToken.unauthenticated(
                "sms", "+8613800138000", null, "login"));

        Assertions.assertTrue(result.isAuthenticated());
        OneTimePasswordIssueResult issueResult = ((OneTimePasswordIssueAuthenticationToken) result).getIssueResult();
        Assertions.assertNotNull(issueResult);
        Assertions.assertDoesNotThrow(() -> UUID.fromString(issueResult.otpTicket()),
                "the ticket id is a bare UUID");
    }

    @Test
    void unsupportedChannelIsMappedToOneTimePasswordUnsupportedChannelException() {
        OneTimePasswordIssueAuthenticationProvider provider = createProvider(
                new DelegatingOneTimePasswordChannel(Collections.emptyMap()));

        Assertions.assertThrows(OneTimePasswordUnsupportedChannelException.class,
                () -> provider.authenticate(OneTimePasswordIssueAuthenticationToken.unauthenticated(
                        "sms", "+8613800138000", null, "login")));
    }

    private OneTimePasswordIssueAuthenticationProvider createProvider(OneTimePasswordChannel oneTimePasswordChannel) {
        return new OneTimePasswordIssueAuthenticationProvider(
                request -> POLICY,
                oneTimePasswordChannel,
                new InMemoryOneTimePasswordService(length -> "1".repeat(length),
                        InMemoryOneTimePasswordService.DEFAULT_MAX_TICKETS, POLICY.maxFailures()),
                null);
    }
}
