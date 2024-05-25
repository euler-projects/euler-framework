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
package org.eulerframework.web.module.authentication.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import org.eulerframework.common.util.Assert;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
import org.eulerframework.web.module.authentication.exception.InvalidSmsResetPinException;
import org.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * 用户密码相关业务逻辑接口
 * @author cFrost
 *
 */
public interface PasswordService {

    /**
     * 获取密码加密器
     * @return 密码加密器
     */
    PasswordEncoder getPasswordEncoder();
    
    /**
     * 获取用户信息相关业务逻辑接口实现类
     * @return 用户信息相关业务逻辑接口实现类
     */
    EulerUserEntityService getEulerUserEntityService();
     
    /**
     * 发送密码重置短信
     * @param mobile 注册手机号，当注册手机号不存在时此方法不做任何响应
     */
    void passwdResetSMSGen(String mobile);

    /**
     * 发送密码重置邮件
     * @param email 注册邮箱，当注册邮箱不存在时此方法不做任何响应
     */
    void passwdResetEmailGen(String email);
    
    /**
     * 从密码重置短信验证码中解析用户ID
     * @param pin 密码重置短信验证码
     * @return 用户ID
     * @throws InvalidSmsResetPinException 密码重置短信验证码不合法
     */
    String analyzeUserIdFromSmsResetPin(String pin)
            throws InvalidSmsResetPinException;
    
    /**
     * 从密码重置邮件token中解析用户ID
     * @param token 密码重置邮件token
     * @return 用户ID
     * @throws InvalidEmailResetTokenException 密码重置邮件token不合法
     */
    String analyzeUserIdFromEmailResetToken(String token)
            throws InvalidEmailResetTokenException;

    /**
     * 校验用户密码，抛出{@link BadCredentialsException}表示密码不正确
     * @param userId 被校验的用户ID
     * @param password 被校验的密码
     * @throws UserNotFoundException 用户不存在
     * @throws BadCredentialsException 密码不正确
     */
    default void checkPassword(String userId, String password) throws UserNotFoundException, BadCredentialsException {
        EulerUserEntity user = this.getEulerUserEntityService().loadUserByUserId(userId);

        if (this.getPasswordEncoder().matches(password.trim(), user.getPassword())) {
            // Password matched successful.
            return;
        }

        throw new BadCredentialsException("Bad Credentials");
    }

    /**
     * 直接更新用户密码
     * @param userId 被更新的用户的ID
     * @param newPassword 新密码
     * @throws UserNotFoundException 用户不存在
     * @throws UserInfoCheckWebException 新密码不符合要求
     */
    default void updatePassword(String userId, String newPassword) throws UserNotFoundException, UserInfoCheckWebException {
        UserDataValidator.validPassword(newPassword);
        EulerUserEntity user = this.getEulerUserEntityService().loadUserByUserId(userId);
        user.setPassword(this.getPasswordEncoder().encode(newPassword));
        this.getEulerUserEntityService().updateUser(user);
    }
    
    /**
     * 验证现密码后更新用户密码
     * @param userId 被更新的用户的ID
     * @param oldPassword 现密码
     * @param newPassword 新密码
     * @throws UserNotFoundException 用户不存在
     * @throws BadCredentialsException 现密码不正确
     * @throws UserInfoCheckWebException 新密码不符合要求
     */
    default void updatePassword(String userId, String oldPassword, String newPassword)
            throws UserNotFoundException, UserInfoCheckWebException {
        this.checkPassword(userId, oldPassword);
        UserDataValidator.validPassword(newPassword);
        EulerUserEntity user = this.getEulerUserEntityService().loadUserByUserId(userId);
        user.setPassword(this.getPasswordEncoder().encode(newPassword.trim()));
        this.getEulerUserEntityService().updateUser(user);
    }
    
    /**
     * 通过密码重置短信验证码重置密码
     * @param pin 密码重置短信验证码
     * @param password 新密码
     * @throws InvalidSmsResetPinException 密码重置短信验证码不合法
     * @throws UserNotFoundException 用户不存在
     * @throws UserInfoCheckWebException 新密码不符合要求
     */
    default void resetPasswordBySmsResetPin(String pin, String password)
            throws InvalidSmsResetPinException, UserNotFoundException, UserInfoCheckWebException {
        Assert.hasText(pin, "A pin is required to reset your password");
        Assert.hasText(password, "New password can not be null");
        
        String userId = this.analyzeUserIdFromSmsResetPin(pin);
        if(!StringUtils.hasText(userId)) {
            throw new InvalidSmsResetPinException();
        }
        
        this.updatePassword(userId, password);
    }

    /**
     * 通过密码重置邮件token重置密码
     * @param token 密码重置邮件token
     * @param password 新密码
     * @throws InvalidEmailResetTokenException 密码重置邮件token不合法
     * @throws UserNotFoundException 用户不存在
     * @throws UserInfoCheckWebException 新密码不符合要求
     */
    default void resetPasswordByEmailResetToken(String token, String password) 
            throws InvalidEmailResetTokenException, UserNotFoundException, UserInfoCheckWebException {
        Assert.hasText(token, "A token is required to reset your password");
        Assert.hasText(password, "New password can not be null");
        
        String userId = this.analyzeUserIdFromEmailResetToken(token);
        if(!StringUtils.hasText(token)) {
            throw new InvalidEmailResetTokenException();
        }
        
        this.updatePassword(userId, password);
    }
}
