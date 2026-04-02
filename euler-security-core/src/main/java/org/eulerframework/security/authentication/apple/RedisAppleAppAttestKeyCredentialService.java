/*
 * Copyright 2013-2026 the original author or authors.
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
package org.eulerframework.security.authentication.apple;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

/**
 * Redis-backed implementation of {@link AppleAppAttestKeyCredentialService}.
 * <p>
 * Key credentials are stored as Redis Hash structures with no TTL (credentials are long-lived).
 * The sign count update uses a Lua script to ensure CAS atomicity.
 */
public class RedisAppleAppAttestKeyCredentialService implements AppleAppAttestKeyCredentialService {

    private static final String DEFAULT_KEY_PREFIX = "app_attest:key:";
    private static final String FIELD_PUBLIC_KEY = "publicKey";
    private static final String FIELD_SIGN_COUNT = "signCount";

    private static final String UPDATE_SIGN_COUNT_SCRIPT =
            "local current = tonumber(redis.call('HGET', KEYS[1], 'signCount')) " +
            "if current ~= nil and tonumber(ARGV[1]) > current then " +
            "  redis.call('HSET', KEYS[1], 'signCount', ARGV[1]) " +
            "  return 1 " +
            "end " +
            "return 0";

    private final StringRedisTemplate redisTemplate;
    private String keyPrefix = DEFAULT_KEY_PREFIX;

    public RedisAppleAppAttestKeyCredentialService(StringRedisTemplate redisTemplate) {
        Assert.notNull(redisTemplate, "redisTemplate must not be null");
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveKeyCredential(AppleAppAttestKeyCredential credential) {
        Assert.notNull(credential, "credential must not be null");
        String key = buildKey(credential.getKeyId());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(credential.getPublicKey().getEncoded());

        this.redisTemplate.opsForHash().putAll(key, Map.of(
                FIELD_PUBLIC_KEY, publicKeyBase64,
                FIELD_SIGN_COUNT, String.valueOf(credential.getSignCount())
        ));
    }

    @Override
    public AppleAppAttestKeyCredential getKeyCredential(String keyId) {
        String key = buildKey(keyId);
        Map<Object, Object> entries = this.redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return null;
        }

        try {
            String publicKeyBase64 = (String) entries.get(FIELD_PUBLIC_KEY);
            String signCountStr = (String) entries.get(FIELD_SIGN_COUNT);

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            long signCount = Long.parseLong(signCountStr);

            return new AppleAppAttestKeyCredential(keyId, publicKey, signCount);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize key credential from Redis", e);
        }
    }

    @Override
    public void updateSignCount(String keyId, long newSignCount) {
        String key = buildKey(keyId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UPDATE_SIGN_COUNT_SCRIPT, Long.class);
        this.redisTemplate.execute(script, Collections.singletonList(key), String.valueOf(newSignCount));
    }

    public void setKeyPrefix(String keyPrefix) {
        Assert.hasText(keyPrefix, "keyPrefix must not be empty");
        this.keyPrefix = keyPrefix;
    }

    private String buildKey(String keyId) {
        return this.keyPrefix + keyId;
    }
}
