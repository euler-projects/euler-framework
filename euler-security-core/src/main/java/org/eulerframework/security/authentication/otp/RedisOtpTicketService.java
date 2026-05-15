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

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Redis-backed {@link OtpTicketService}.
 * <p>
 * Each ticket is stored as a Redis hash under the key
 * {@code <prefix><ticketId>} (default prefix {@code euler:otp:ticket:}) with a
 * TTL aligned to {@link OtpTicket#expiresAt()}. Suitable for clustered
 * deployments where multiple server instances must share ticket state.
 *
 * @see InMemoryOtpTicketService
 * @see JdbcOtpTicketService
 */
public class RedisOtpTicketService implements OtpTicketService {

    private static final String DEFAULT_KEY_PREFIX = "euler:otp:ticket:";
    private static final int DEFAULT_MAX_FAILURES = 5;

    private static final String FIELD_TICKET_ID             = "ticket_id";
    private static final String FIELD_CHANNEL               = "channel";
    private static final String FIELD_RECIPIENT             = "recipient";
    private static final String FIELD_PURPOSE               = "purpose";
    private static final String FIELD_OTP                   = "otp";
    private static final String FIELD_CODE_CHALLENGE        = "code_challenge";
    private static final String FIELD_CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String FIELD_EXPIRES_AT            = "expires_at";
    private static final String FIELD_FAILURE_COUNT         = "failure_count";
    private static final String FIELD_CONSUMED              = "consumed";

    private final StringRedisTemplate redisTemplate;
    private final int maxFailures;
    private String keyPrefix = DEFAULT_KEY_PREFIX;

    public RedisOtpTicketService(StringRedisTemplate redisTemplate) {
        this(redisTemplate, DEFAULT_MAX_FAILURES);
    }

    public RedisOtpTicketService(StringRedisTemplate redisTemplate, int maxFailures) {
        Assert.notNull(redisTemplate, "redisTemplate must not be null");
        Assert.isTrue(maxFailures > 0, "maxFailures must be positive");
        this.redisTemplate = redisTemplate;
        this.maxFailures = maxFailures;
    }

    public void setKeyPrefix(String keyPrefix) {
        Assert.hasText(keyPrefix, "keyPrefix must not be empty");
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void save(OtpTicket ticket) {
        Assert.notNull(ticket, "ticket must not be null");
        String key = buildKey(ticket.ticketId());
        Map<String, String> fields = new HashMap<>();
        fields.put(FIELD_TICKET_ID, ticket.ticketId());
        fields.put(FIELD_CHANNEL, ticket.channel());
        fields.put(FIELD_RECIPIENT, ticket.recipient());
        if (ticket.purpose() != null) {
            fields.put(FIELD_PURPOSE, ticket.purpose());
        }
        fields.put(FIELD_OTP, ticket.otp());
        fields.put(FIELD_CODE_CHALLENGE, ticket.codeChallenge());
        fields.put(FIELD_CODE_CHALLENGE_METHOD, ticket.codeChallengeMethod());
        fields.put(FIELD_EXPIRES_AT, Long.toString(ticket.expiresAt().toEpochMilli()));
        fields.put(FIELD_FAILURE_COUNT, Integer.toString(ticket.failureCount()));
        fields.put(FIELD_CONSUMED, ticket.consumed() ? "1" : "0");

        this.redisTemplate.opsForHash().putAll(key, fields);
        Duration ttl = Duration.between(Instant.now(), ticket.expiresAt());
        if (!ttl.isNegative() && !ttl.isZero()) {
            this.redisTemplate.expire(key, ttl);
        }
    }

    @Override
    public OtpVerification consume(String ticketId, String codeVerifier, String otp, String expectedPurpose) {
        if (ticketId == null) {
            return null;
        }
        String key = buildKey(ticketId);
        HashOperations<String, Object, Object> hashOps = this.redisTemplate.opsForHash();
        Map<Object, Object> raw = hashOps.entries(key);
        if (raw.isEmpty()) {
            return null;
        }

        OtpTicket ticket = readTicket(raw);
        if (ticket.consumed() || Instant.now().isAfter(ticket.expiresAt())) {
            this.redisTemplate.delete(key);
            return null;
        }

        boolean otpMatches = Objects.equals(ticket.otp(), otp);
        boolean pkceMatches = OtpPkceVerifier.verify(codeVerifier, ticket.codeChallenge(), ticket.codeChallengeMethod());
        boolean purposeMatches = expectedPurpose == null
                || Objects.equals(expectedPurpose, ticket.purpose());

        if (otpMatches && pkceMatches && purposeMatches) {
            Boolean deleted = this.redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                return new OtpVerification(ticket.ticketId(), ticket.channel(), ticket.recipient(),
                        ticket.purpose(), Instant.now());
            }
            return null;
        }

        Long updated = hashOps.increment(key, FIELD_FAILURE_COUNT, 1L);
        if (updated != null && updated >= this.maxFailures) {
            this.redisTemplate.delete(key);
        }
        return null;
    }

    private OtpTicket readTicket(Map<Object, Object> raw) {
        String purpose = stringValue(raw.get(FIELD_PURPOSE));
        Instant expiresAt = Instant.ofEpochMilli(Long.parseLong(stringValue(raw.get(FIELD_EXPIRES_AT))));
        int failureCount = Integer.parseInt(stringValue(raw.get(FIELD_FAILURE_COUNT)));
        boolean consumed = "1".equals(stringValue(raw.get(FIELD_CONSUMED)));
        return new OtpTicket(
                stringValue(raw.get(FIELD_TICKET_ID)),
                stringValue(raw.get(FIELD_CHANNEL)),
                stringValue(raw.get(FIELD_RECIPIENT)),
                purpose,
                stringValue(raw.get(FIELD_OTP)),
                stringValue(raw.get(FIELD_CODE_CHALLENGE)),
                stringValue(raw.get(FIELD_CODE_CHALLENGE_METHOD)),
                expiresAt,
                failureCount,
                consumed);
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String buildKey(String ticketId) {
        return this.keyPrefix + ticketId;
    }
}
