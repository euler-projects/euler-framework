/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.module.authentication.service;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.eulerframework.common.email.ThreadSimpleMailSender;
import org.eulerframework.common.util.CommonUtils;
import org.eulerframework.common.util.jwt.InvalidJwtException;
import org.eulerframework.common.util.jwt.Jwt;
import org.eulerframework.common.util.jwt.JwtEncryptor;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
import org.eulerframework.web.module.authentication.exception.InvalidSmsResetPinException;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.vo.UserResetJwtClaims;
import org.eulerframework.web.util.ServletUtils;

/**
 * @author cFrost
 *
 */
@Service("passwordService")
public class PasswordServiceImpl implements PasswordService {

    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private EulerUserEntityService eulerUserEntityService;
    @Resource
    private JwtEncryptor jwtEncryptor;
    @Autowired(required = false)
    private String resetPasswordEmailSubject = "[Euler Projects] Please reset your password";
    @Autowired(required = false)
    private String resetPasswordEmailContent = 
        "<p>You can use the following link to reset your password in 10 minutes:</p>" +
        "<p><a href=\"${resetPasswordUrl}\">${resetPasswordUrl}</a></p>";

    @Autowired(required = false)
    private ThreadSimpleMailSender threadSimpleMailSender;

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    @Override
    public EulerUserEntityService getEulerUserEntityService() {
        return this.eulerUserEntityService;
    }

    @Override
    public void passwdResetSMSGen(String mobile) {
        // TODO Auto-generated method stub
    }

    @Override
    public void passwdResetEmailGen(String email) {
        EulerUserEntity user;
        try {
            user = this.eulerUserEntityService.loadUserByEmail(email);
        } catch (UserNotFoundException e) {
            CommonUtils.sleep(1);
            return;
        }

        UserResetJwtClaims claims = new UserResetJwtClaims(user, 30 * 60);
        Jwt jwt = this.jwtEncryptor.encode(claims);
        String token = jwt.getEncoded();
        String resetPasswordUrl = WebConfig.getWebUrl() + ServletUtils.getRequest().getContextPath()
                + "/reset-password?type=EMAIL&token=" + token;
        String content = this.resetPasswordEmailContent.replaceAll("\\$\\{resetPasswordUrl\\}", resetPasswordUrl);
        if (this.threadSimpleMailSender != null) {
            this.threadSimpleMailSender.send(this.resetPasswordEmailSubject, content, email);
        } else {
            System.out.println(content);
        }
    }

    @Override
    public String analyzeUserIdFromSmsResetPin(String pin) throws InvalidSmsResetPinException {
        // TODO Auto-generated method stub
        throw new InvalidSmsResetPinException();
    }

    @Override
    public String analyzeUserIdFromEmailResetToken(String token) throws InvalidEmailResetTokenException {
        UserResetJwtClaims claims;
        try {
            claims = this.jwtEncryptor.decode(token, UserResetJwtClaims.class);
        } catch (InvalidJwtException e) {
            throw new InvalidEmailResetTokenException();
        }
        return claims.getUserId();
    }

}
