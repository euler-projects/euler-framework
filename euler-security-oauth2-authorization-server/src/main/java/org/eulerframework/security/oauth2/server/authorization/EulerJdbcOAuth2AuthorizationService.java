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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.security.jackson2.EulerSecurityObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.List;
import java.util.function.Function;

public class EulerJdbcOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public EulerJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        super(jdbcOperations, registeredClientRepository);
        this.mixIn();
    }

    public EulerJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository, LobHandler lobHandler) {
        super(jdbcOperations, registeredClientRepository, lobHandler);
        this.mixIn();
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        try {
            super.save(authorization);
        } catch (Exception e) {
            throw this.logAndRethrowException(e);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        try {
            super.remove(authorization);
        } catch (Exception e) {
            throw this.logAndRethrowException(e);
        }
    }

    @Override
    public OAuth2Authorization findById(String id) {
        try {
            return super.findById(id);
        } catch (Exception e) {
            throw this.logAndRethrowException(e);
        }
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        try {
            return super.findByToken(token, tokenType);
        } catch (Exception e) {
            throw this.logAndRethrowException(e);
        }
    }

    private RuntimeException logAndRethrowException(Exception e) {
        this.logger.warn("JdbcOAuth2AuthorizationService Exception: {}", e.getMessage(), e);
        return ExceptionUtils.rethrow(e);
    }

    private void mixIn() {
        RowMapper<OAuth2Authorization> authorizationRowMapper = this.getAuthorizationRowMapper();
        if (authorizationRowMapper instanceof OAuth2AuthorizationRowMapper) {
            ((OAuth2AuthorizationRowMapper) authorizationRowMapper).setObjectMapper(EulerSecurityObjectMapperFactory.getInstance());
                    //.getObjectMapper().registerModule(new EulerOAuth2AuthorizationServerJackson2Module());
        }
        Function<OAuth2Authorization, List<SqlParameterValue>> authorizationParametersMapper = this.getAuthorizationParametersMapper();
        if (authorizationParametersMapper instanceof OAuth2AuthorizationParametersMapper) {
            ((OAuth2AuthorizationParametersMapper) authorizationParametersMapper).setObjectMapper(EulerSecurityObjectMapperFactory.getInstance());
                    //.getObjectMapper().registerModule(new EulerOAuth2AuthorizationServerJackson2Module());
        }
    }
}
