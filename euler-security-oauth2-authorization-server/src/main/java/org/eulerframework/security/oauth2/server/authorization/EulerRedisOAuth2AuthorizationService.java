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

import org.eulerframework.security.oauth2.server.authorization.jackson.EulerOAuth2JsonMapper;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class EulerRedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final static String PROP_REGISTERED_CLIENT_ID = "registered_client_id";
    private final static String PROP_PRINCIPAL_NAME = "principal_name";
    private final static String PROP_AUTHORIZATION_GRANT_TYPE = "authorization_grant_type";
    private final static String PROP_AUTHORIZED_SCOPES = "authorized_scopes";
    private final static String PROP_ATTRIBUTES = "attributes";
    private final static String PROP_STATE = "state";

    private final static String PROP_AUTHORIZATION_CODE_VALUE = "authorization_code_value";
    private final static String PROP_AUTHORIZATION_CODE_IAT = "authorization_code_iat";
    private final static String PROP_AUTHORIZATION_CODE_EXP = "authorization_code_exp";
    private final static String PROP_AUTHORIZATION_CODE_METADATA = "authorization_code_metadata";

    private final static String PROP_ACCESS_TOKEN_VALUE = "access_token_value";
    private final static String PROP_ACCESS_TOKEN_IAT = "access_token_iat";
    private final static String PROP_ACCESS_TOKEN_EXP = "access_token_exp";
    private final static String PROP_ACCESS_TOKEN_METADATA = "access_token_metadata";
    private final static String PROP_ACCESS_TOKEN_TYPE = "access_token_type";
    private final static String PROP_ACCESS_TOKEN_SCOPES = "access_token_scopes";

    private final static String PROP_OIDC_ID_TOKEN_VALUE = "oidc_id_token_value";
    private final static String PROP_OIDC_ID_TOKEN_IAT = "oidc_id_token_iat";
    private final static String PROP_OIDC_ID_TOKEN_EXP = "oidc_id_token_exp";
    private final static String PROP_OIDC_ID_TOKEN_METADATA = "oidc_id_token_metadata";

    private final static String PROP_REFRESH_TOKEN_VALUE = "refresh_token_value";
    private final static String PROP_REFRESH_TOKEN_IAT = "refresh_token_iat";
    private final static String PROP_REFRESH_TOKEN_EXP = "refresh_token_exp";
    private final static String PROP_REFRESH_TOKEN_METADATA = "refresh_token_metadata";

    private final static String PROP_USER_CODE_VALUE = "user_code_value";
    private final static String PROP_USER_CODE_IAT = "user_code_iat";
    private final static String PROP_USER_CODE_EXP = "user_code_exp";
    private final static String PROP_USER_CODE_METADATA = "user_code_metadata";

    private final static String PROP_DEVICE_CODE_VALUE = "device_code_value";
    private final static String PROP_DEVICE_CODE_IAT = "device_code_iat";
    private final static String PROP_DEVICE_CODE_EXP = "device_code_exp";
    private final static String PROP_DEVICE_CODE_METADATA = "device_code_metadata";

    private final static String INDEX_STATE = "state_index";
    private final static String INDEX_AUTHORIZATION_CODE = "authorization_code_index";
    private final static String INDEX_ACCESS_TOKEN = "access_token_index";
    private final static String INDEX_OIDC_ID_TOKEN = "oidc_id_token_index";
    private final static String INDEX_REFRESH_TOKEN = "refresh_token_index";
    private final static String INDEX_USER_CODE = "user_code_index";
    private final static String INDEX_DEVICE_CODE = "device_code_index";

    private String keyPrefix = "oauth2:auth:";
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper;
    private final RegisteredClientRepository registeredClientRepository;
    private final Duration expireTime;

    public EulerRedisOAuth2AuthorizationService(StringRedisTemplate stringRedisTemplate, RegisteredClientRepository registeredClientRepository, Duration expireTime) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jsonMapper = EulerOAuth2JsonMapper.getInstance();
        this.registeredClientRepository = registeredClientRepository;
        this.expireTime = expireTime;
    }

    public void setKeyPrefix(String keyPrefix) {
        Assert.hasText(keyPrefix, "keyPrefix must not be empty");
        if (!keyPrefix.endsWith(":")) {
            keyPrefix += ":";
        }
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        OAuth2Authorization existsAuthorization = this.findById(authorization.getId());
        if (existsAuthorization != null) {
            this.deleteAuthorization(existsAuthorization);
        }
        this.saveAuthorization(authorization);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        OAuth2Authorization existsAuthorization = this.findById(authorization.getId());
        // 这里优先使用已保存的Authorization数据确保可以完全删除Token索引数据
        this.deleteAuthorization(existsAuthorization == null ? authorization : existsAuthorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id must not be empty");

        String registeredClientId = this.getAuthorizationStringProperty(PROP_REGISTERED_CLIENT_ID, id);

        if (registeredClientId == null) {
            return null;
        }

        RegisteredClient registeredClient = this.registeredClientRepository.findById(registeredClientId);

        if (registeredClient == null) {
            throw new DataRetrievalFailureException(
                    "The RegisteredClient with id '" + registeredClientId + "' was not found in the RegisteredClientRepository.");
        }

        String principalName = this.getAuthorizationStringProperty(PROP_PRINCIPAL_NAME, id);
        String authorizationGrantType = this.getAuthorizationStringProperty(PROP_AUTHORIZATION_GRANT_TYPE, id);

        Set<String> authorizedScopes = Collections.emptySet();
        String authorizedScopesString = this.getAuthorizationStringProperty(PROP_AUTHORIZED_SCOPES, id);
        if (authorizedScopesString != null) {
            authorizedScopes = StringUtils.commaDelimitedListToSet(authorizedScopesString);
        }

        Map<String, Object> attributes = this.getAuthorizationMapProperty(PROP_ATTRIBUTES, id);

        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(id)
                .principalName(principalName)
                .authorizationGrantType(new AuthorizationGrantType(authorizationGrantType))
                .authorizedScopes(authorizedScopes)
                .attributes((attrs) -> attrs.putAll(attributes));

        String state = this.getAuthorizationStringProperty(PROP_STATE, id);
        if (StringUtils.hasText(state)) {
            builder.attribute(OAuth2ParameterNames.STATE, state);
        }

        Instant tokenIssuedAt;
        Instant tokenExpiresAt;
        String authorizationCodeValue = this.getAuthorizationStringProperty(PROP_AUTHORIZATION_CODE_VALUE, id);
        if (StringUtils.hasText(authorizationCodeValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(PROP_AUTHORIZATION_CODE_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(PROP_AUTHORIZATION_CODE_EXP, id);
            Map<String, Object> authorizationCodeMetadata = this.getAuthorizationMapProperty(PROP_AUTHORIZATION_CODE_METADATA, id);

            OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
                    authorizationCodeValue, tokenIssuedAt, tokenExpiresAt);
            builder.token(authorizationCode, (metadata) -> metadata.putAll(authorizationCodeMetadata));
        }

        String accessTokenValue = getAuthorizationStringProperty(PROP_ACCESS_TOKEN_VALUE, id);
        if (StringUtils.hasText(accessTokenValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(PROP_ACCESS_TOKEN_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(PROP_ACCESS_TOKEN_EXP, id);
            Map<String, Object> accessTokenMetadata = this.getAuthorizationMapProperty(PROP_ACCESS_TOKEN_METADATA, id);
            OAuth2AccessToken.TokenType tokenType;
            String tokenTypeValue = this.getAuthorizationStringProperty(PROP_ACCESS_TOKEN_TYPE, id);
            if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(tokenTypeValue)) {
                tokenType = OAuth2AccessToken.TokenType.BEARER;
            } else {
                throw new BadCredentialsException("invalid access token type: " + tokenTypeValue);
            }

            Set<String> scopes = Collections.emptySet();
            String accessTokenScopes = this.getAuthorizationStringProperty(PROP_ACCESS_TOKEN_SCOPES, id);
            if (accessTokenScopes != null) {
                scopes = StringUtils.commaDelimitedListToSet(accessTokenScopes);
            }
            OAuth2AccessToken accessToken = new OAuth2AccessToken(tokenType, accessTokenValue, tokenIssuedAt, tokenExpiresAt, scopes);
            builder.token(accessToken, (metadata) -> metadata.putAll(accessTokenMetadata));
        }

        String oidcIdTokenValue = this.getAuthorizationStringProperty(PROP_OIDC_ID_TOKEN_VALUE, id);
        if (StringUtils.hasText(oidcIdTokenValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(PROP_OIDC_ID_TOKEN_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(PROP_OIDC_ID_TOKEN_EXP, id);
            Map<String, Object> oidcTokenMetadata = this.getAuthorizationMapProperty(PROP_OIDC_ID_TOKEN_METADATA, id);

            @SuppressWarnings("unchecked")
            OidcIdToken oidcToken = new OidcIdToken(
                    oidcIdTokenValue, tokenIssuedAt, tokenExpiresAt, (Map<String, Object>) oidcTokenMetadata.get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME));
            builder.token(oidcToken, (metadata) -> metadata.putAll(oidcTokenMetadata));
        }

        String refreshTokenValue = this.getAuthorizationStringProperty(PROP_REFRESH_TOKEN_VALUE, id);
        if (StringUtils.hasText(refreshTokenValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(PROP_REFRESH_TOKEN_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(PROP_REFRESH_TOKEN_EXP, id);
            Map<String, Object> refreshTokenMetadata = this.getAuthorizationMapProperty(PROP_REFRESH_TOKEN_METADATA, id);

            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                    refreshTokenValue, tokenIssuedAt, tokenExpiresAt);
            builder.token(refreshToken, (metadata) -> metadata.putAll(refreshTokenMetadata));
        }

        String userCodeValue = this.getAuthorizationStringProperty(PROP_USER_CODE_VALUE, id);
        if (StringUtils.hasText(userCodeValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(PROP_USER_CODE_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(PROP_USER_CODE_EXP, id);
            Map<String, Object> userCodeMetadata = this.getAuthorizationMapProperty(PROP_USER_CODE_METADATA, id);

            OAuth2UserCode userCode = new OAuth2UserCode(userCodeValue, tokenIssuedAt, tokenExpiresAt);
            builder.token(userCode, (metadata) -> metadata.putAll(userCodeMetadata));
        }

        String deviceCodeValue = this.getAuthorizationStringProperty(PROP_DEVICE_CODE_VALUE, id);
        if (StringUtils.hasText(deviceCodeValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(PROP_DEVICE_CODE_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(PROP_DEVICE_CODE_EXP, id);
            Map<String, Object> deviceCodeMetadata = this.getAuthorizationMapProperty(PROP_DEVICE_CODE_METADATA, id);

            OAuth2DeviceCode deviceCode = new OAuth2DeviceCode(deviceCodeValue, tokenIssuedAt, tokenExpiresAt);
            builder.token(deviceCode, (metadata) -> metadata.putAll(deviceCodeMetadata));
        }

        return builder.build();
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        String tokenHash = this.genTokenHash(token);
        List<String> keys = new ArrayList<>();
        if (tokenType == null) {
            keys.add(this.genTokenIndexKey(INDEX_AUTHORIZATION_CODE, tokenHash));
            keys.add(this.genTokenIndexKey(INDEX_ACCESS_TOKEN, tokenHash));
            keys.add(this.genTokenIndexKey(INDEX_OIDC_ID_TOKEN, tokenHash));
            keys.add(this.genTokenIndexKey(INDEX_REFRESH_TOKEN, tokenHash));
            keys.add(this.genTokenIndexKey(INDEX_USER_CODE, tokenHash));
            keys.add(this.genTokenIndexKey(INDEX_DEVICE_CODE, tokenHash));
        } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            keys.add(this.genTokenIndexKey(INDEX_STATE, tokenHash));
        } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            keys.add(this.genTokenIndexKey(INDEX_AUTHORIZATION_CODE, tokenHash));
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            keys.add(this.genTokenIndexKey(INDEX_ACCESS_TOKEN, tokenHash));
        } else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
            keys.add(this.genTokenIndexKey(INDEX_OIDC_ID_TOKEN, tokenHash));
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            keys.add(this.genTokenIndexKey(INDEX_REFRESH_TOKEN, tokenHash));
        } else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
            keys.add(this.genTokenIndexKey(INDEX_USER_CODE, tokenHash));
        } else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
            keys.add(this.genTokenIndexKey(INDEX_DEVICE_CODE, tokenHash));
        }

        for (String key : keys) {
            String id = this.stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(id)) {
                return this.findById(id);
            }
        }

        return null;
    }

    private void deleteAuthorization(OAuth2Authorization authorization) {
        List<String> keys = this.genAllPropertyKeys(authorization);
        this.stringRedisTemplate.delete(keys);
    }

    private void saveAuthorization(OAuth2Authorization authorization) {
        String id = authorization.getId();
        this.saveAuthorizationProperty(PROP_REGISTERED_CLIENT_ID, id, authorization.getRegisteredClientId());
        this.saveAuthorizationProperty(PROP_PRINCIPAL_NAME, id, authorization.getPrincipalName());
        this.saveAuthorizationProperty(PROP_AUTHORIZATION_GRANT_TYPE, id, authorization.getAuthorizationGrantType().getValue());
        this.saveAuthorizationProperty(PROP_AUTHORIZED_SCOPES, id, StringUtils.collectionToCommaDelimitedString(authorization.getAuthorizedScopes()));
        this.saveAuthorizationProperty(PROP_ATTRIBUTES, id, authorization.getAttributes());

        String state = authorization.getAttribute(OAuth2ParameterNames.STATE);
        if (StringUtils.hasText(state)) {
            this.saveAuthorizationProperty(PROP_STATE, id, state);
            this.saveTokenIndex(INDEX_STATE, state, authorization);
        }

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationToken = authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationToken != null) {
            OAuth2AuthorizationCode authorizationCode = authorizationToken.getToken();
            this.saveAuthorizationProperty(PROP_AUTHORIZATION_CODE_VALUE, id, authorizationCode.getTokenValue());
            this.saveAuthorizationProperty(PROP_AUTHORIZATION_CODE_IAT, id, authorizationCode.getIssuedAt());
            this.saveAuthorizationProperty(PROP_AUTHORIZATION_CODE_EXP, id, authorizationCode.getExpiresAt());
            this.saveAuthorizationProperty(PROP_AUTHORIZATION_CODE_METADATA, id, authorizationToken.getMetadata());
            this.saveTokenIndex(INDEX_AUTHORIZATION_CODE, authorizationCode.getTokenValue(), authorization);
        }

        OAuth2Authorization.Token<OAuth2AccessToken> auth2AccessTokenToken = authorization.getToken(OAuth2AccessToken.class);
        if (auth2AccessTokenToken != null) {
            OAuth2AccessToken accessToken = auth2AccessTokenToken.getToken();
            this.saveAuthorizationProperty(PROP_ACCESS_TOKEN_VALUE, id, accessToken.getTokenValue());
            this.saveAuthorizationProperty(PROP_ACCESS_TOKEN_IAT, id, accessToken.getIssuedAt());
            this.saveAuthorizationProperty(PROP_ACCESS_TOKEN_EXP, id, accessToken.getExpiresAt());
            this.saveAuthorizationProperty(PROP_ACCESS_TOKEN_METADATA, id, auth2AccessTokenToken.getMetadata());
            this.saveTokenIndex(INDEX_ACCESS_TOKEN, accessToken.getTokenValue(), authorization);

            this.saveAuthorizationProperty(PROP_ACCESS_TOKEN_TYPE, id, accessToken.getTokenType().getValue());
            this.saveAuthorizationProperty(PROP_ACCESS_TOKEN_SCOPES, id, StringUtils.collectionToCommaDelimitedString(accessToken.getScopes()));
        }

        OAuth2Authorization.Token<OidcIdToken> oidcIdTokenToken = authorization.getToken(OidcIdToken.class);
        if (oidcIdTokenToken != null) {
            OidcIdToken oidcIdToken = oidcIdTokenToken.getToken();
            this.saveAuthorizationProperty(PROP_OIDC_ID_TOKEN_VALUE, id, oidcIdToken.getTokenValue());
            this.saveAuthorizationProperty(PROP_OIDC_ID_TOKEN_IAT, id, oidcIdToken.getIssuedAt());
            this.saveAuthorizationProperty(PROP_OIDC_ID_TOKEN_EXP, id, oidcIdToken.getExpiresAt());
            this.saveAuthorizationProperty(PROP_OIDC_ID_TOKEN_METADATA, id, oidcIdTokenToken.getMetadata());
            this.saveTokenIndex(INDEX_OIDC_ID_TOKEN, oidcIdToken.getTokenValue(), authorization);
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshTokenToken = authorization.getToken(OAuth2RefreshToken.class);
        if (refreshTokenToken != null) {
            OAuth2RefreshToken refreshToken = refreshTokenToken.getToken();
            this.saveAuthorizationProperty(PROP_REFRESH_TOKEN_VALUE, id, refreshToken.getTokenValue());
            this.saveAuthorizationProperty(PROP_REFRESH_TOKEN_IAT, id, refreshToken.getIssuedAt());
            this.saveAuthorizationProperty(PROP_REFRESH_TOKEN_EXP, id, refreshToken.getExpiresAt());
            this.saveAuthorizationProperty(PROP_REFRESH_TOKEN_METADATA, id, refreshTokenToken.getMetadata());
            this.saveTokenIndex(INDEX_REFRESH_TOKEN, refreshToken.getTokenValue(), authorization);
        }

        OAuth2Authorization.Token<OAuth2UserCode> userCodeToken = authorization.getToken(OAuth2UserCode.class);
        if (userCodeToken != null) {
            OAuth2UserCode userCode = userCodeToken.getToken();
            this.saveAuthorizationProperty(PROP_USER_CODE_VALUE, id, userCode.getTokenValue());
            this.saveAuthorizationProperty(PROP_USER_CODE_IAT, id, userCode.getIssuedAt());
            this.saveAuthorizationProperty(PROP_USER_CODE_EXP, id, userCode.getExpiresAt());
            this.saveAuthorizationProperty(PROP_USER_CODE_METADATA, id, userCodeToken.getMetadata());
            this.saveTokenIndex(INDEX_USER_CODE, userCode.getTokenValue(), authorization);
        }

        OAuth2Authorization.Token<OAuth2DeviceCode> deviceCodeToken = authorization.getToken(OAuth2DeviceCode.class);
        if (deviceCodeToken != null) {
            OAuth2DeviceCode deviceCode = deviceCodeToken.getToken();
            this.saveAuthorizationProperty(PROP_DEVICE_CODE_VALUE, id, deviceCode.getTokenValue());
            this.saveAuthorizationProperty(PROP_DEVICE_CODE_IAT, id, deviceCode.getIssuedAt());
            this.saveAuthorizationProperty(PROP_DEVICE_CODE_EXP, id, deviceCode.getExpiresAt());
            this.saveAuthorizationProperty(PROP_DEVICE_CODE_METADATA, id, deviceCodeToken.getMetadata());
            this.saveTokenIndex(INDEX_DEVICE_CODE, deviceCode.getTokenValue(), authorization);
        }
    }

    private void saveAuthorizationProperty(String property, String authorizationId, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        this.setStringValue(this.genPropertyKey(property, authorizationId), value);
    }

    private void saveAuthorizationProperty(String property, String authorizationId, Map<String, Object> map) {
        if (CollectionUtils.isEmpty(map)) {
            return;
        }
        this.saveAuthorizationProperty(property, authorizationId, this.jsonMapper.writeValueAsString(map));
    }

    private void saveAuthorizationProperty(String property, String authorizationId, Instant instant) {
        if (instant == null) {
            return;
        }
        this.saveAuthorizationProperty(property, authorizationId, String.valueOf(instant.toEpochMilli()));
    }

    private void saveTokenIndex(String indexName, String tokenValue, OAuth2Authorization authorization) {
        if (tokenValue == null) {
            return;
        }
        this.setStringValue(this.genTokenIndexKey(indexName, this.genTokenHash(tokenValue)), authorization.getId());
    }

    private String getAuthorizationStringProperty(String property, String authorizationId) {
        return this.stringRedisTemplate.opsForValue().get(this.genPropertyKey(property, authorizationId));
    }

    private Map<String, Object> getAuthorizationMapProperty(String property, String authorizationId) {
        return Optional.ofNullable(this.getAuthorizationStringProperty(property, authorizationId))
                .map(a -> this.jsonMapper.readValue(a, new TypeReference<Map<String, Object>>() {
                }))
                .orElse(null);
    }

    private Instant getAuthorizationStringInstantProperty(String property, String authorizationId) {
        return Optional.ofNullable(this.getAuthorizationStringProperty(property, authorizationId))
                .map(Long::parseLong)
                .map(Instant::ofEpochMilli)
                .orElse(null);
    }

    private String genTokenHash(String token) {
        if (token == null) {
            return null;
        }
        if (token.length() <= 64 && !org.apache.commons.lang3.StringUtils.containsWhitespace(token)) {
            return token;
        }
        return DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8)) + "|" + token.length();
    }

    private <T extends OAuth2Token> Optional<String> genTokenIndexKey(OAuth2Authorization authorization, Class<T> tokenClass, String indexName) {
        return Optional.ofNullable(authorization.getToken(tokenClass))
                .map(OAuth2Authorization.Token::getToken)
                .map(T::getTokenValue)
                .filter(StringUtils::hasText)
                .map(this::genTokenHash)
                .map(tokenHash -> this.genTokenIndexKey(indexName, tokenHash));
    }

    private String genTokenIndexKey(String indexName, String tokenHash) {
        return this.keyPrefix + indexName + ":" + tokenHash;
    }

    private String genPropertyKey(String property, String authorizationId) {
        return this.keyPrefix + property + ":" + authorizationId;
    }

    private List<String> genAllPropertyKeys(OAuth2Authorization authorization) {
        String id = authorization.getId();
        List<String> keys = new ArrayList<>();
        keys.add(this.genPropertyKey(PROP_REGISTERED_CLIENT_ID, id));
        keys.add(this.genPropertyKey(PROP_PRINCIPAL_NAME, id));
        keys.add(this.genPropertyKey(PROP_AUTHORIZATION_GRANT_TYPE, id));
        keys.add(this.genPropertyKey(PROP_AUTHORIZED_SCOPES, id));
        keys.add(this.genPropertyKey(PROP_ATTRIBUTES, id));
        keys.add(this.genPropertyKey(PROP_STATE, id));
        keys.add(this.genPropertyKey(PROP_AUTHORIZATION_CODE_VALUE, id));
        keys.add(this.genPropertyKey(PROP_AUTHORIZATION_CODE_IAT, id));
        keys.add(this.genPropertyKey(PROP_AUTHORIZATION_CODE_EXP, id));
        keys.add(this.genPropertyKey(PROP_AUTHORIZATION_CODE_METADATA, id));
        keys.add(this.genPropertyKey(PROP_ACCESS_TOKEN_VALUE, id));
        keys.add(this.genPropertyKey(PROP_ACCESS_TOKEN_IAT, id));
        keys.add(this.genPropertyKey(PROP_ACCESS_TOKEN_EXP, id));
        keys.add(this.genPropertyKey(PROP_ACCESS_TOKEN_METADATA, id));
        keys.add(this.genPropertyKey(PROP_ACCESS_TOKEN_TYPE, id));
        keys.add(this.genPropertyKey(PROP_ACCESS_TOKEN_SCOPES, id));
        keys.add(this.genPropertyKey(PROP_OIDC_ID_TOKEN_VALUE, id));
        keys.add(this.genPropertyKey(PROP_OIDC_ID_TOKEN_IAT, id));
        keys.add(this.genPropertyKey(PROP_OIDC_ID_TOKEN_EXP, id));
        keys.add(this.genPropertyKey(PROP_OIDC_ID_TOKEN_METADATA, id));
        keys.add(this.genPropertyKey(PROP_REFRESH_TOKEN_VALUE, id));
        keys.add(this.genPropertyKey(PROP_REFRESH_TOKEN_IAT, id));
        keys.add(this.genPropertyKey(PROP_REFRESH_TOKEN_EXP, id));
        keys.add(this.genPropertyKey(PROP_REFRESH_TOKEN_METADATA, id));
        keys.add(this.genPropertyKey(PROP_USER_CODE_VALUE, id));
        keys.add(this.genPropertyKey(PROP_USER_CODE_IAT, id));
        keys.add(this.genPropertyKey(PROP_USER_CODE_EXP, id));
        keys.add(this.genPropertyKey(PROP_USER_CODE_METADATA, id));
        keys.add(this.genPropertyKey(PROP_DEVICE_CODE_VALUE, id));
        keys.add(this.genPropertyKey(PROP_DEVICE_CODE_IAT, id));
        keys.add(this.genPropertyKey(PROP_DEVICE_CODE_EXP, id));
        keys.add(this.genPropertyKey(PROP_DEVICE_CODE_METADATA, id));

        String state = authorization.getAttribute(OAuth2ParameterNames.STATE);
        if (StringUtils.hasText(state)) {
            keys.add(this.genTokenIndexKey(INDEX_STATE, this.genTokenHash(state)));
        }
        this.genTokenIndexKey(authorization, OAuth2AuthorizationCode.class, INDEX_AUTHORIZATION_CODE).ifPresent(keys::add);
        this.genTokenIndexKey(authorization, OAuth2AccessToken.class, INDEX_ACCESS_TOKEN).ifPresent(keys::add);
        this.genTokenIndexKey(authorization, OidcIdToken.class, INDEX_OIDC_ID_TOKEN).ifPresent(keys::add);
        this.genTokenIndexKey(authorization, OAuth2RefreshToken.class, INDEX_REFRESH_TOKEN).ifPresent(keys::add);
        this.genTokenIndexKey(authorization, OAuth2UserCode.class, INDEX_USER_CODE).ifPresent(keys::add);
        this.genTokenIndexKey(authorization, OAuth2DeviceCode.class, INDEX_DEVICE_CODE).ifPresent(keys::add);

        return keys;
    }

    private void setStringValue(String key, String value) {
        this.stringRedisTemplate.opsForValue().set(key, value, this.expireTime);
    }
}
