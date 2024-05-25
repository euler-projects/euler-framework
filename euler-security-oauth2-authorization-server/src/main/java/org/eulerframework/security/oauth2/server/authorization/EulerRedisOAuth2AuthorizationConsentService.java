/*
 * Copyright 2013-2024 the original author or authors.
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
package org.eulerframework.security.oauth2.server.authorization;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;

public class EulerRedisOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {
    private final static String KEY_AUTHORITIES = "consent:authorities";

    private String keyPrefix = "oauth2:auth:";
    private final StringRedisTemplate stringRedisTemplate;
    private final RegisteredClientRepository registeredClientRepository;
    private final Duration expireTime;

    public EulerRedisOAuth2AuthorizationConsentService(StringRedisTemplate stringRedisTemplate, RegisteredClientRepository registeredClientRepository, Duration expireTime) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.registeredClientRepository = registeredClientRepository;
        this.expireTime = expireTime;
    }

    public void setKeyPrefix(String keyPrefix) {
        Assert.hasText(keyPrefix, "keyPrefix must not be empty");
        if(!keyPrefix.endsWith(":")) {
            keyPrefix += ":";
        }
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
        String key = this.genKey(authorizationConsent.getRegisteredClientId(), authorizationConsent.getPrincipalName());
        String authorities = StringUtils.collectionToCommaDelimitedString(authorizationConsent.getAuthorities());
        this.stringRedisTemplate.opsForValue().set(key, authorities, this.expireTime);
    }

    @Override
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
        String key = this.genKey(authorizationConsent.getRegisteredClientId(), authorizationConsent.getPrincipalName());
        this.stringRedisTemplate.delete(key);

    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        RegisteredClient registeredClient = this.registeredClientRepository.findById(registeredClientId);
        if (registeredClient == null) {
            throw new DataRetrievalFailureException(
                    "The RegisteredClient with id '" + registeredClientId + "' was not found in the RegisteredClientRepository.");
        }

        OAuth2AuthorizationConsent.Builder builder = OAuth2AuthorizationConsent.withId(registeredClientId, principalName);

        String key = this.genKey(registeredClientId, principalName);
        String authorities = this.stringRedisTemplate.opsForValue().get(key);

        if (!StringUtils.hasText(authorities)) {
            return null;
        }

        for (String authority : StringUtils.commaDelimitedListToSet(authorities)) {
            builder.authority(new SimpleGrantedAuthority(authority));
        }

        return builder.build();
    }

    private String genKey(String registeredClientId, String principalName) {
        Assert.hasText(registeredClientId, "registeredClientId cannot be empty");
        Assert.hasText(principalName, "principalName cannot be empty");
        return this.keyPrefix + KEY_AUTHORITIES + ":" + registeredClientId + ":" + principalName;
    }
}
