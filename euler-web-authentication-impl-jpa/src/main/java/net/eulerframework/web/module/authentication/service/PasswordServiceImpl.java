/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://github.com/euler-projects/euler-framework
 * https://cfrost.net
 */
package net.eulerframework.web.module.authentication.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.stereotype.Service;

import net.eulerframework.common.email.ThreadSimpleMailSender;
import net.eulerframework.common.util.CommonUtils;
import net.eulerframework.common.util.jwt.InvalidJwtException;
import net.eulerframework.common.util.jwt.JwtEncryptor;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
import net.eulerframework.web.module.authentication.exception.InvalidSmsResetPinException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.vo.UserResetJwtClaims;
import net.eulerframework.web.util.ServletUtils;

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
    @Resource
    private String resetPasswordEmailSubject;
    @Resource
    private String resetPasswordEmailContent;

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
