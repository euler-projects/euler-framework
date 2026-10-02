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

import org.eulerframework.resource.Tag;
import org.eulerframework.security.core.EulerAuthority;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

class OneTimePasswordAuthenticationProviderTests {

    private static final String CHANNEL = "sms";
    private static final String RECIPIENT = "+8613800138000";
    private static final String OTP = "123456";

    @Test
    void authenticatesAgainstAnExistingIdentity() {
        InMemoryOneTimePasswordService ticketService = newTicketService();
        String ticketId = ticketService.generate(request(CHANNEL)).ticketId();
        RecordingIdentityService identityService = new RecordingIdentityService(
                Optional.of(identity("usr_1")));
        RecordingUserService userService = new RecordingUserService();
        OneTimePasswordAuthenticationProvider provider = new OneTimePasswordAuthenticationProvider(
                ticketService, identityService, userService,
                type -> JitProvisioningPolicy.disabled());

        Authentication result = provider.authenticate(
                new OneTimePasswordAuthenticationToken(ticketId, OTP));

        Assertions.assertTrue(result.isAuthenticated());
        OneTimePasswordAuthentication authenticated = (OneTimePasswordAuthentication) result;
        Assertions.assertEquals("usr_1", authenticated.getUserIdentity().getUserId());
        Assertions.assertEquals("alice", authenticated.getPrincipal().getUsername());
        Assertions.assertNull(userService.created);
        Assertions.assertTrue(authenticated.getAuthorities().stream()
                        .anyMatch(authority -> "FACTOR_OTP".equals(authority.getAuthority())),
                "the result carries the OTP authentication factor");

        // The ticket is consumed exactly once.
        Assertions.assertNull(ticketService.consume(
                new OneTimePasswordAuthenticationToken(ticketId, OTP)));
    }

    @Test
    void rejectsAnIncorrectOtpValue() {
        InMemoryOneTimePasswordService ticketService = newTicketService();
        String ticketId = ticketService.generate(request(CHANNEL)).ticketId();
        OneTimePasswordAuthenticationProvider provider = new OneTimePasswordAuthenticationProvider(
                ticketService, new RecordingIdentityService(Optional.empty()),
                new RecordingUserService(), type -> JitProvisioningPolicy.disabled());

        Assertions.assertThrows(BadCredentialsException.class, () ->
                provider.authenticate(new OneTimePasswordAuthenticationToken(ticketId, "000000")));
    }

    @Test
    void provisionsAnUnknownRecipientWhenJitIsEnabled() {
        InMemoryOneTimePasswordService ticketService = newTicketService();
        String ticketId = ticketService.generate(request(CHANNEL)).ticketId();
        RecordingIdentityService identityService = new RecordingIdentityService(Optional.empty());
        RecordingUserService userService = new RecordingUserService();
        OneTimePasswordAuthenticationProvider provider = new OneTimePasswordAuthenticationProvider(
                ticketService, identityService, userService,
                type -> JitProvisioningPolicy.enabled(List.of(EulerAuthority.USER)));

        Authentication result = provider.authenticate(
                new OneTimePasswordAuthenticationToken(ticketId, OTP));

        Assertions.assertTrue(result.isAuthenticated());
        Assertions.assertNotNull(userService.created);
        Assertions.assertNotNull(identityService.createdPrototype);
        Assertions.assertEquals("phone", identityService.createdPrototype.getIdentityType());
        Assertions.assertEquals(RECIPIENT,
                identityService.createdPrototype.getProperties().get("phone"));
    }

    @Test
    void rejectsAnUnknownRecipientWhenJitIsDisabled() {
        InMemoryOneTimePasswordService ticketService = newTicketService();
        String ticketId = ticketService.generate(request(CHANNEL)).ticketId();
        RecordingUserService userService = new RecordingUserService();
        OneTimePasswordAuthenticationProvider provider = new OneTimePasswordAuthenticationProvider(
                ticketService, new RecordingIdentityService(Optional.empty()),
                userService, type -> JitProvisioningPolicy.disabled());

        Assertions.assertThrows(BadCredentialsException.class, () ->
                provider.authenticate(new OneTimePasswordAuthenticationToken(ticketId, OTP)));
        Assertions.assertNull(userService.created);
    }

    @Test
    void rejectsAChannelWithNoIdentityMapping() {
        InMemoryOneTimePasswordService ticketService = newTicketService();
        String ticketId = ticketService.generate(request("fax")).ticketId();
        OneTimePasswordAuthenticationProvider provider = new OneTimePasswordAuthenticationProvider(
                ticketService, new RecordingIdentityService(Optional.empty()),
                new RecordingUserService(), type -> JitProvisioningPolicy.disabled());

        Assertions.assertThrows(OneTimePasswordUnsupportedChannelException.class, () ->
                provider.authenticate(new OneTimePasswordAuthenticationToken(ticketId, OTP)));
    }

    // ---------- fixtures ----------

    private static InMemoryOneTimePasswordService newTicketService() {
        return new InMemoryOneTimePasswordService(length -> OTP, InMemoryOneTimePasswordService.DEFAULT_MAX_TICKETS, 5);
    }

    private static GenerateOneTimePasswordRequest request(String channel) {
        return new GenerateOneTimePasswordRequest(channel, RECIPIENT, "login", 6, Duration.ofMinutes(5), OTP);
    }

    private static UserIdentity identity(String userId) {
        return UserIdentity.builder()
                .identityId("idn_1")
                .identityType("phone")
                .subject("subject-hash")
                .userId(userId)
                .boundAt(Instant.now())
                .build();
    }

    private static EulerUser user(String userId, String username) {
        return new EulerUser() {
            @Override
            public String getUserId() {
                return userId;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public Collection<? extends EulerAuthority> getAuthorities() {
                return List.of(authority(EulerAuthority.USER));
            }

            @Override
            public Collection<Tag> getTags() {
                return List.of();
            }

            @Override
            public String getPassword() {
                return "{noop}secret";
            }

            @Override
            public void eraseCredentials() {
            }

            @Override
            public void reloadUserDetails(EulerUserDetails userDetails) {
            }
        };
    }

    private static EulerAuthority authority(String name) {
        return new EulerAuthority() {
            @Override
            public String getAuthority() {
                return name;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return name;
            }
        };
    }

    private static class RecordingUserService implements EulerUserService {

        private EulerUserDetails created;
        private int nextId = 1;

        @Override
        public EulerUser createUser(EulerUserDetails userDetails) {
            this.created = userDetails;
            return user("usr_" + this.nextId++, userDetails.getUsername());
        }

        @Override
        public EulerUser createUser(EulerUser eulerUser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EulerUser loadUserById(String userId) {
            return user(userId, "alice");
        }

        @Override
        public EulerUser loadUserByUsername(String username) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<EulerUser> listUsers(int offset, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateUser(EulerUser eulerUser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updatePassword(String userId, String newPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteUser(String userId) {
            throw new UnsupportedOperationException();
        }
    }

    private static class RecordingIdentityService implements UserIdentityService {

        private final Optional<UserIdentity> existing;
        private UserIdentity createdPrototype;
        private final List<UserIdentity> created = new ArrayList<>();

        private RecordingIdentityService(Optional<UserIdentity> existing) {
            this.existing = existing;
        }

        @Override
        public String identityType() {
            return "phone";
        }

        @Override
        public UserIdentity createUserIdentity(String userId, MultiValueMap<String, String> params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserIdentity createUserIdentity(String userId, UserIdentity prototype) {
            this.createdPrototype = prototype;
            UserIdentity persisted = UserIdentity.builder()
                    .identityId("idn_" + (this.created.size() + 1))
                    .identityType(prototype.getIdentityType())
                    .subject("subject-hash")
                    .userId(userId)
                    .boundAt(Instant.now())
                    .build();
            this.created.add(persisted);
            return persisted;
        }

        @Override
        public Optional<UserIdentity> getUserIdentity(String userId, String identityId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UserIdentity> listUserIdentities(String userId) {
            return List.of();
        }

        @Override
        public List<UserIdentity> listUserIdentities(String userId, String identityType) {
            return List.of();
        }

        @Override
        public UserIdentity updateUserIdentity(String userId, String identityId, MultiValueMap<String, String> params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserIdentity updateUserIdentity(String userId, String identityId, UserIdentity prototype) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteUserIdentity(String userId, String identityId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<UserIdentity> findUserIdentityByRawSubject(String identityType, String rawSubject) {
            return this.existing;
        }

        @Override
        public Optional<String> getRawFieldValue(String userId, String identityId, String fieldName) {
            return Optional.empty();
        }
    }
}
