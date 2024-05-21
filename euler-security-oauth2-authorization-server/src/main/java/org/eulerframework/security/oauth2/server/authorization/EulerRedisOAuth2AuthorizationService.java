package org.eulerframework.security.oauth2.server.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.EulerOAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class EulerRedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final static String KEY_REGISTERED_CLIENT_ID = "registered_client_id";
    private final static String KEY_PRINCIPAL_NAME = "principal_name";
    private final static String KEY_AUTHORIZATION_GRANT_TYPE = "authorization_grant_type";
    private final static String KEY_AUTHORIZED_SCOPES = "authorized_scopes";
    private final static String KEY_ATTRIBUTES = "attributes";
    private final static String KEY_STATE = "state";

    private final static String KEY_AUTHORIZATION_CODE_VALUE = "authorization_code_value";
    private final static String KEY_AUTHORIZATION_CODE_IAT = "authorization_code_iat";
    private final static String KEY_AUTHORIZATION_CODE_EXP = "authorization_code_exp";
    private final static String KEY_AUTHORIZATION_CODE_METADATA = "authorization_code_metadata";

    private final static String KEY_ACCESS_TOKEN_VALUE = "access_token_value";
    private final static String KEY_ACCESS_TOKEN_IAT = "access_token_iat";
    private final static String KEY_ACCESS_TOKEN_EXP = "access_token_exp";
    private final static String KEY_ACCESS_TOKEN_METADATA = "access_token_metadata";
    private final static String KEY_ACCESS_TOKEN_TYPE = "access_token_type";
    private final static String KEY_ACCESS_TOKEN_SCOPES = "access_token_scopes";

    private final static String KEY_OIDC_ID_TOKEN_VALUE = "oidc_id_token_value";
    private final static String KEY_OIDC_ID_TOKEN_IAT = "oidc_id_token_iat";
    private final static String KEY_OIDC_ID_TOKEN_EXP = "oidc_id_token_exp";
    private final static String KEY_OIDC_ID_TOKEN_METADATA = "oidc_id_token_metadata";

    private final static String KEY_REFRESH_TOKEN_VALUE = "refresh_token_value";
    private final static String KEY_REFRESH_TOKEN_IAT = "refresh_token_iat";
    private final static String KEY_REFRESH_TOKEN_EXP = "refresh_token_exp";
    private final static String KEY_REFRESH_TOKEN_METADATA = "refresh_token_metadata";

    private final static String KEY_USER_CODE_VALUE = "user_code_value";
    private final static String KEY_USER_CODE_IAT = "user_code_iat";
    private final static String KEY_USER_CODE_EXP = "user_code_exp";
    private final static String KEY_USER_CODE_METADATA = "user_code_metadata";

    private final static String KEY_DEVICE_CODE_VALUE = "device_code_value";
    private final static String KEY_DEVICE_CODE_IAT = "device_code_iat";
    private final static String KEY_DEVICE_CODE_EXP = "device_code_exp";
    private final static String KEY_DEVICE_CODE_METADATA = "device_code_metadata";

    private final static String INDEX_KEY_STATE = "state_index";
    private final static String INDEX_KEY_AUTHORIZATION_CODE = "authorization_code_index";
    private final static String INDEX_KEY_ACCESS_TOKEN = "access_token_index";
    private final static String INDEX_KEY_OIDC_ID_TOKEN = "oidc_id_token_index";
    private final static String INDEX_KEY_REFRESH_TOKEN = "refresh_token_index";
    private final static String INDEX_KEY_USER_CODE = "user_code_index";
    private final static String INDEX_KEY_DEVICE_CODE = "device_code_index";

    private String keyPrefix = "oauth2:auth:";
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RegisteredClientRepository registeredClientRepository;
    private final Duration expireTime;

    public EulerRedisOAuth2AuthorizationService(StringRedisTemplate stringRedisTemplate, RegisteredClientRepository registeredClientRepository, Duration expireTime) {
        this.stringRedisTemplate = stringRedisTemplate;
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        this.objectMapper.registerModules(securityModules);
        this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        this.objectMapper.registerModule(new EulerOAuth2AuthorizationServerJackson2Module());
        this.registeredClientRepository = registeredClientRepository;
        this.expireTime = expireTime;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        Assert.notNull(authorization.getId(), "authorization id cannot be null");
        this.saveAuthorization(authorization);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        Assert.notNull(authorization.getId(), "authorization id cannot be null");
        List<String> keys = this.getKeys(authorization);
        this.stringRedisTemplate.delete(keys);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return this.getAuthorization(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        String tokenHash = this.hash(token);
        List<String> keys = new ArrayList<>();
        if (tokenType == null) {
            keys.add(this.tokenIndexKey(INDEX_KEY_AUTHORIZATION_CODE, tokenHash));
            keys.add(this.tokenIndexKey(INDEX_KEY_ACCESS_TOKEN, tokenHash));
            keys.add(this.tokenIndexKey(INDEX_KEY_OIDC_ID_TOKEN, tokenHash));
            keys.add(this.tokenIndexKey(INDEX_KEY_REFRESH_TOKEN, tokenHash));
            keys.add(this.tokenIndexKey(INDEX_KEY_USER_CODE, tokenHash));
            keys.add(this.tokenIndexKey(INDEX_KEY_DEVICE_CODE, tokenHash));
        } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            keys.add(this.tokenIndexKey(INDEX_KEY_STATE, tokenHash));
        } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            keys.add(this.tokenIndexKey(INDEX_KEY_AUTHORIZATION_CODE, tokenHash));
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            keys.add(this.tokenIndexKey(INDEX_KEY_ACCESS_TOKEN, tokenHash));
        } else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
            keys.add(this.tokenIndexKey(INDEX_KEY_OIDC_ID_TOKEN, tokenHash));
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            keys.add(this.tokenIndexKey(INDEX_KEY_REFRESH_TOKEN, tokenHash));
        } else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
            keys.add(this.tokenIndexKey(INDEX_KEY_USER_CODE, tokenHash));
        } else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
            keys.add(this.tokenIndexKey(INDEX_KEY_DEVICE_CODE, tokenHash));
        }

        for (String key : keys) {
            String id = this.stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(id)) {
                return this.findById(id);
            }
        }

        return null;
    }

    private String hash(String token) {
        if (token == null) {
            return null;
        }

        if (token.length() <= 64 && !org.apache.commons.lang3.StringUtils.containsWhitespace(token)) {
            return token;
        }
        return DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8)) + "|" + token.length();
    }

    public void saveAuthorization(OAuth2Authorization authorization) {
        String id = authorization.getId();
        this.saveAuthorizationProperty(KEY_REGISTERED_CLIENT_ID, id, authorization.getRegisteredClientId());
        this.saveAuthorizationProperty(KEY_PRINCIPAL_NAME, id, authorization.getPrincipalName());
        this.saveAuthorizationProperty(KEY_AUTHORIZATION_GRANT_TYPE, id, authorization.getAuthorizationGrantType().getValue());
        this.saveAuthorizationProperty(KEY_AUTHORIZED_SCOPES, id, StringUtils.collectionToCommaDelimitedString(authorization.getAuthorizedScopes()));
        this.saveAuthorizationProperty(KEY_ATTRIBUTES, id, authorization.getAttributes());

        String state = authorization.getAttribute(OAuth2ParameterNames.STATE);
        if (StringUtils.hasText(state)) {
            this.saveAuthorizationProperty(KEY_STATE, id, state);
            this.saveTokenIndex(INDEX_KEY_STATE, this.hash(state), id);
        }

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationToken = authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationToken != null) {
            OAuth2AuthorizationCode authorizationCode = authorizationToken.getToken();
            this.saveAuthorizationProperty(KEY_AUTHORIZATION_CODE_VALUE, id, authorizationCode.getTokenValue());
            this.saveAuthorizationProperty(KEY_AUTHORIZATION_CODE_IAT, id, authorizationCode.getIssuedAt());
            this.saveAuthorizationProperty(KEY_AUTHORIZATION_CODE_EXP, id, authorizationCode.getExpiresAt());
            this.saveAuthorizationProperty(KEY_AUTHORIZATION_CODE_METADATA, id, authorizationToken.getMetadata());
            this.saveTokenIndex(INDEX_KEY_AUTHORIZATION_CODE, this.hash(authorizationCode.getTokenValue()), id);
        }

        OAuth2Authorization.Token<OAuth2AccessToken> auth2AccessTokenToken = authorization.getToken(OAuth2AccessToken.class);
        if (auth2AccessTokenToken != null) {
            OAuth2AccessToken accessToken = auth2AccessTokenToken.getToken();
            this.saveAuthorizationProperty(KEY_ACCESS_TOKEN_VALUE, id, accessToken.getTokenValue());
            this.saveAuthorizationProperty(KEY_ACCESS_TOKEN_TYPE, id, accessToken.getTokenType().getValue());
            this.saveAuthorizationProperty(KEY_ACCESS_TOKEN_SCOPES, id, StringUtils.collectionToCommaDelimitedString(accessToken.getScopes()));
            this.saveAuthorizationProperty(KEY_ACCESS_TOKEN_IAT, id, accessToken.getIssuedAt());
            this.saveAuthorizationProperty(KEY_ACCESS_TOKEN_EXP, id, accessToken.getExpiresAt());
            this.saveAuthorizationProperty(KEY_ACCESS_TOKEN_METADATA, id, auth2AccessTokenToken.getMetadata());
            this.saveTokenIndex(INDEX_KEY_ACCESS_TOKEN, this.hash(accessToken.getTokenValue()), id);
        }

        OAuth2Authorization.Token<OidcIdToken> oidcIdTokenToken = authorization.getToken(OidcIdToken.class);
        if (oidcIdTokenToken != null) {
            OidcIdToken oidcIdToken = oidcIdTokenToken.getToken();
            this.saveAuthorizationProperty(KEY_OIDC_ID_TOKEN_VALUE, id, oidcIdToken.getTokenValue());
            this.saveAuthorizationProperty(KEY_OIDC_ID_TOKEN_IAT, id, oidcIdToken.getIssuedAt());
            this.saveAuthorizationProperty(KEY_OIDC_ID_TOKEN_EXP, id, oidcIdToken.getExpiresAt());
            this.saveAuthorizationProperty(KEY_OIDC_ID_TOKEN_METADATA, id, oidcIdTokenToken.getMetadata());
            this.saveTokenIndex(INDEX_KEY_OIDC_ID_TOKEN, this.hash(oidcIdToken.getTokenValue()), id);
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshTokenToken = authorization.getToken(OAuth2RefreshToken.class);
        if (refreshTokenToken != null) {
            OAuth2RefreshToken refreshToken = refreshTokenToken.getToken();
            this.saveAuthorizationProperty(KEY_REFRESH_TOKEN_VALUE, id, refreshToken.getTokenValue());
            this.saveAuthorizationProperty(KEY_REFRESH_TOKEN_IAT, id, refreshToken.getIssuedAt());
            this.saveAuthorizationProperty(KEY_REFRESH_TOKEN_EXP, id, refreshToken.getExpiresAt());
            this.saveAuthorizationProperty(KEY_REFRESH_TOKEN_METADATA, id, refreshTokenToken.getMetadata());
            this.saveTokenIndex(INDEX_KEY_REFRESH_TOKEN, this.hash(refreshToken.getTokenValue()), id);
        }

        OAuth2Authorization.Token<OAuth2UserCode> userCodeToken = authorization.getToken(OAuth2UserCode.class);
        if (userCodeToken != null) {
            OAuth2UserCode userCode = userCodeToken.getToken();
            this.saveAuthorizationProperty(KEY_USER_CODE_VALUE, id, userCode.getTokenValue());
            this.saveAuthorizationProperty(KEY_USER_CODE_IAT, id, userCode.getIssuedAt());
            this.saveAuthorizationProperty(KEY_USER_CODE_EXP, id, userCode.getExpiresAt());
            this.saveAuthorizationProperty(KEY_USER_CODE_METADATA, id, userCodeToken.getMetadata());
            this.saveTokenIndex(INDEX_KEY_USER_CODE, this.hash(userCode.getTokenValue()), id);
        }

        OAuth2Authorization.Token<OAuth2DeviceCode> deviceCodeToken = authorization.getToken(OAuth2DeviceCode.class);
        if (deviceCodeToken != null) {
            OAuth2DeviceCode deviceCode = deviceCodeToken.getToken();
            this.saveAuthorizationProperty(KEY_DEVICE_CODE_VALUE, id, deviceCode.getTokenValue());
            this.saveAuthorizationProperty(KEY_DEVICE_CODE_IAT, id, deviceCode.getIssuedAt());
            this.saveAuthorizationProperty(KEY_DEVICE_CODE_EXP, id, deviceCode.getExpiresAt());
            this.saveAuthorizationProperty(KEY_DEVICE_CODE_METADATA, id, deviceCodeToken.getMetadata());
            this.saveTokenIndex(INDEX_KEY_DEVICE_CODE, this.hash(deviceCode.getTokenValue()), id);
        }
    }

    public OAuth2Authorization getAuthorization(String id) {
        String registeredClientId = this.getAuthorizationStringProperty(KEY_REGISTERED_CLIENT_ID, id);
        RegisteredClient registeredClient = this.registeredClientRepository.findById(registeredClientId);
        if (registeredClient == null) {
            throw new DataRetrievalFailureException(
                    "The RegisteredClient with id '" + registeredClientId + "' was not found in the RegisteredClientRepository.");
        }

        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient);
        String principalName = this.getAuthorizationStringProperty(KEY_PRINCIPAL_NAME, id);
        String authorizationGrantType = this.getAuthorizationStringProperty(KEY_AUTHORIZATION_GRANT_TYPE, id);
        Set<String> authorizedScopes = Collections.emptySet();
        String authorizedScopesString = this.getAuthorizationStringProperty(KEY_AUTHORIZED_SCOPES, id);
        if (authorizedScopesString != null) {
            authorizedScopes = StringUtils.commaDelimitedListToSet(authorizedScopesString);
        }
        Map<String, Object> attributes = this.getAuthorizationMapProperty(KEY_ATTRIBUTES, id);

        builder.id(id)
                .principalName(principalName)
                .authorizationGrantType(new AuthorizationGrantType(authorizationGrantType))
                .authorizedScopes(authorizedScopes)
                .attributes((attrs) -> attrs.putAll(attributes));

        String state = this.getAuthorizationStringProperty(KEY_STATE, id);
        if (StringUtils.hasText(state)) {
            builder.attribute(OAuth2ParameterNames.STATE, state);
        }

        Instant tokenIssuedAt;
        Instant tokenExpiresAt;
        String authorizationCodeValue = this.getAuthorizationStringProperty(KEY_AUTHORIZATION_CODE_VALUE, id);

        if (StringUtils.hasText(authorizationCodeValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(KEY_AUTHORIZATION_CODE_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(KEY_AUTHORIZATION_CODE_EXP, id);
            Map<String, Object> authorizationCodeMetadata = this.getAuthorizationMapProperty(KEY_AUTHORIZATION_CODE_METADATA, id);

            OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
                    authorizationCodeValue, tokenIssuedAt, tokenExpiresAt);
            builder.token(authorizationCode, (metadata) -> metadata.putAll(authorizationCodeMetadata));
        }

        String accessTokenValue = getAuthorizationStringProperty(KEY_ACCESS_TOKEN_VALUE, id);
        if (StringUtils.hasText(accessTokenValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(KEY_ACCESS_TOKEN_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(KEY_ACCESS_TOKEN_EXP, id);
            Map<String, Object> accessTokenMetadata = this.getAuthorizationMapProperty(KEY_ACCESS_TOKEN_METADATA, id);
            OAuth2AccessToken.TokenType tokenType = null;
            if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(this.getAuthorizationStringProperty(KEY_ACCESS_TOKEN_TYPE, id))) {
                tokenType = OAuth2AccessToken.TokenType.BEARER;
            }

            Set<String> scopes = Collections.emptySet();
            String accessTokenScopes = this.getAuthorizationStringProperty(KEY_ACCESS_TOKEN_SCOPES, id);
            if (accessTokenScopes != null) {
                scopes = StringUtils.commaDelimitedListToSet(accessTokenScopes);
            }
            OAuth2AccessToken accessToken = new OAuth2AccessToken(tokenType, accessTokenValue, tokenIssuedAt, tokenExpiresAt, scopes);
            builder.token(accessToken, (metadata) -> metadata.putAll(accessTokenMetadata));
        }

        String oidcIdTokenValue = this.getAuthorizationStringProperty(KEY_OIDC_ID_TOKEN_VALUE, id);
        if (StringUtils.hasText(oidcIdTokenValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(KEY_OIDC_ID_TOKEN_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(KEY_OIDC_ID_TOKEN_EXP, id);
            Map<String, Object> oidcTokenMetadata = this.getAuthorizationMapProperty(KEY_OIDC_ID_TOKEN_METADATA, id);

            OidcIdToken oidcToken = new OidcIdToken(
                    oidcIdTokenValue, tokenIssuedAt, tokenExpiresAt, (Map<String, Object>) oidcTokenMetadata.get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME));
            builder.token(oidcToken, (metadata) -> metadata.putAll(oidcTokenMetadata));
        }

        String refreshTokenValue = this.getAuthorizationStringProperty(KEY_REFRESH_TOKEN_VALUE, id);
        if (StringUtils.hasText(refreshTokenValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(KEY_REFRESH_TOKEN_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(KEY_REFRESH_TOKEN_EXP, id);
            Map<String, Object> refreshTokenMetadata = this.getAuthorizationMapProperty(KEY_REFRESH_TOKEN_METADATA, id);

            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                    refreshTokenValue, tokenIssuedAt, tokenExpiresAt);
            builder.token(refreshToken, (metadata) -> metadata.putAll(refreshTokenMetadata));
        }

        String userCodeValue = this.getAuthorizationStringProperty(KEY_USER_CODE_VALUE, id);
        if (StringUtils.hasText(userCodeValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(KEY_USER_CODE_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(KEY_USER_CODE_EXP, id);
            Map<String, Object> userCodeMetadata = this.getAuthorizationMapProperty(KEY_USER_CODE_METADATA, id);

            OAuth2UserCode userCode = new OAuth2UserCode(userCodeValue, tokenIssuedAt, tokenExpiresAt);
            builder.token(userCode, (metadata) -> metadata.putAll(userCodeMetadata));
        }

        String deviceCodeValue = this.getAuthorizationStringProperty(KEY_DEVICE_CODE_VALUE, id);
        if (StringUtils.hasText(deviceCodeValue)) {
            tokenIssuedAt = this.getAuthorizationStringInstantProperty(KEY_DEVICE_CODE_IAT, id);
            tokenExpiresAt = this.getAuthorizationStringInstantProperty(KEY_DEVICE_CODE_EXP, id);
            Map<String, Object> deviceCodeMetadata = this.getAuthorizationMapProperty(KEY_DEVICE_CODE_METADATA, id);

            OAuth2DeviceCode deviceCode = new OAuth2DeviceCode(deviceCodeValue, tokenIssuedAt, tokenExpiresAt);
            builder.token(deviceCode, (metadata) -> metadata.putAll(deviceCodeMetadata));
        }

        return builder.build();
    }

    private void saveAuthorizationProperty(String key, String id, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        this.setStringValue(this.keyPrefix + key + ":" + id, value);
    }

    private void saveAuthorizationProperty(String key, String id, Map<String, Object> map) {
        if (CollectionUtils.isEmpty(map)) {
            return;
        }
        try {
            this.saveAuthorizationProperty(key, id, this.objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            ExceptionUtils.<RuntimeException>rethrow(e);
        }
    }

    private void saveAuthorizationProperty(String key, String id, Instant instant) {
        if (instant == null) {
            return;
        }

        this.saveAuthorizationProperty(key, id, String.valueOf(instant.toEpochMilli()));
    }

    private String getAuthorizationStringProperty(String key, String id) {
        return this.stringRedisTemplate.opsForValue().get(this.keyPrefix + key + ":" + id);
    }

    private Map<String, Object> getAuthorizationMapProperty(String key, String id) {
        return Optional.ofNullable(this.getAuthorizationStringProperty(key, id))
                .map(a -> {
                    try {
                        return this.objectMapper.readValue(a, new TypeReference<Map<String, Object>>() {
                        });
                    } catch (JsonProcessingException e) {
                        throw ExceptionUtils.<RuntimeException>rethrow(e);
                    }
                })
                .orElse(null);
    }

    private Instant getAuthorizationStringInstantProperty(String key, String id) {
        return Optional.ofNullable(this.getAuthorizationStringProperty(key, id))
                .map(Long::parseLong)
                .map(Instant::ofEpochMilli)
                .orElse(null);
    }

    private List<String> getKeys(OAuth2Authorization authorization) {
        String id = authorization.getId();
        List<String> keys = new ArrayList<>();
        keys.add(this.keyPrefix + KEY_REGISTERED_CLIENT_ID + ":" + id);
        keys.add(this.keyPrefix + KEY_PRINCIPAL_NAME + ":" + id);
        keys.add(this.keyPrefix + KEY_AUTHORIZATION_GRANT_TYPE + ":" + id);
        keys.add(this.keyPrefix + KEY_AUTHORIZED_SCOPES + ":" + id);
        keys.add(this.keyPrefix + KEY_ATTRIBUTES + ":" + id);
        keys.add(this.keyPrefix + KEY_STATE + ":" + id);
        keys.add(this.keyPrefix + KEY_AUTHORIZATION_CODE_VALUE + ":" + id);
        keys.add(this.keyPrefix + KEY_AUTHORIZATION_CODE_IAT + ":" + id);
        keys.add(this.keyPrefix + KEY_AUTHORIZATION_CODE_EXP + ":" + id);
        keys.add(this.keyPrefix + KEY_AUTHORIZATION_CODE_METADATA + ":" + id);
        keys.add(this.keyPrefix + KEY_ACCESS_TOKEN_VALUE + ":" + id);
        keys.add(this.keyPrefix + KEY_ACCESS_TOKEN_IAT + ":" + id);
        keys.add(this.keyPrefix + KEY_ACCESS_TOKEN_EXP + ":" + id);
        keys.add(this.keyPrefix + KEY_ACCESS_TOKEN_METADATA + ":" + id);
        keys.add(this.keyPrefix + KEY_ACCESS_TOKEN_TYPE + ":" + id);
        keys.add(this.keyPrefix + KEY_ACCESS_TOKEN_SCOPES + ":" + id);
        keys.add(this.keyPrefix + KEY_OIDC_ID_TOKEN_VALUE + ":" + id);
        keys.add(this.keyPrefix + KEY_OIDC_ID_TOKEN_IAT + ":" + id);
        keys.add(this.keyPrefix + KEY_OIDC_ID_TOKEN_EXP + ":" + id);
        keys.add(this.keyPrefix + KEY_OIDC_ID_TOKEN_METADATA + ":" + id);
        keys.add(this.keyPrefix + KEY_REFRESH_TOKEN_VALUE + ":" + id);
        keys.add(this.keyPrefix + KEY_REFRESH_TOKEN_IAT + ":" + id);
        keys.add(this.keyPrefix + KEY_REFRESH_TOKEN_EXP + ":" + id);
        keys.add(this.keyPrefix + KEY_REFRESH_TOKEN_METADATA + ":" + id);
        keys.add(this.keyPrefix + KEY_USER_CODE_VALUE + ":" + id);
        keys.add(this.keyPrefix + KEY_USER_CODE_IAT + ":" + id);
        keys.add(this.keyPrefix + KEY_USER_CODE_EXP + ":" + id);
        keys.add(this.keyPrefix + KEY_USER_CODE_METADATA + ":" + id);
        keys.add(this.keyPrefix + KEY_DEVICE_CODE_VALUE + ":" + id);
        keys.add(this.keyPrefix + KEY_DEVICE_CODE_IAT + ":" + id);
        keys.add(this.keyPrefix + KEY_DEVICE_CODE_EXP + ":" + id);
        keys.add(this.keyPrefix + KEY_DEVICE_CODE_METADATA + ":" + id);

        String state = authorization.getAttribute(OAuth2ParameterNames.STATE);
        if (StringUtils.hasText(state)) {
            keys.add(this.tokenIndexKey(INDEX_KEY_STATE, this.hash(state)));
        }
        this.tokenIndexKey(authorization, OAuth2AuthorizationCode.class, INDEX_KEY_AUTHORIZATION_CODE).ifPresent(keys::add);
        this.tokenIndexKey(authorization, OAuth2AccessToken.class, INDEX_KEY_ACCESS_TOKEN).ifPresent(keys::add);
        this.tokenIndexKey(authorization, OidcIdToken.class, INDEX_KEY_OIDC_ID_TOKEN).ifPresent(keys::add);
        this.tokenIndexKey(authorization, OAuth2RefreshToken.class, INDEX_KEY_REFRESH_TOKEN).ifPresent(keys::add);
        this.tokenIndexKey(authorization, OAuth2UserCode.class, INDEX_KEY_USER_CODE).ifPresent(keys::add);
        this.tokenIndexKey(authorization, OAuth2DeviceCode.class, INDEX_KEY_DEVICE_CODE).ifPresent(keys::add);

        return keys;
    }

    private <T extends OAuth2Token> Optional<String> tokenIndexKey(OAuth2Authorization authorization, Class<T> tokenClass, String key) {
        return Optional.ofNullable(authorization.getToken(tokenClass))
                .map(OAuth2Authorization.Token::getToken)
                .map(T::getTokenValue)
                .filter(StringUtils::hasText)
                .map(this::hash)
                .map(tokenHash -> this.tokenIndexKey(key, tokenHash));
    }

    private String tokenIndexKey(String key, String tokenHash) {
        Assert.hasText(tokenHash, "tokenHash cannot be empty");
        return this.keyPrefix + key + ":" + tokenHash;
    }

    private void saveTokenIndex(String key, String tokenHash, String tokenIndex) {
        this.setStringValue(this.tokenIndexKey(key, tokenHash), tokenIndex);
    }

    private void setStringValue(String key, String value) {
        this.stringRedisTemplate.opsForValue().set(key, value, this.expireTime);
    }
}
