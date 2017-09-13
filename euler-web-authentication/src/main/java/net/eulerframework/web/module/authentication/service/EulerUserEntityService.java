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
 * https://github.com/euler-form/web-form
 * https://cfrost.net
 */
package net.eulerframework.web.module.authentication.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * @author cFrost
 *
 */
public interface EulerUserEntityService {

    PasswordEncoder getPasswordEncoder();

    EulerUserEntity loadUserByUserId(String userId) throws UserNotFoundException;

    EulerUserEntity loadUserByUsername(String username) throws UserNotFoundException;

    EulerUserEntity loadUserByEmail(String email) throws UserNotFoundException;

    EulerUserEntity loadUserByMobile(String mobile) throws UserNotFoundException;

    void updateUser(EulerUserEntity eulerUserEntity);

    default void updateUsername(String userId, String newUsername) throws UserNotFoundException {
        UserDataValidator.validUsername(newUsername);
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setUsername(newUsername.trim());
        this.updateUser(user);
    }

    default void updateEmail(String userId, String newEmail) throws UserNotFoundException {
        UserDataValidator.validEmail(newEmail);
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setEmail(newEmail.trim());
        this.updateUser(user);
    }

    default void updateMobile(String userId, String newMobile) throws UserNotFoundException {
        UserDataValidator.validMobile(newMobile);
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setMobile(newMobile.trim());
        this.updateUser(user);
    }

    default void checkPassword(String userId, String password) throws UserNotFoundException, BadCredentialsException {
        EulerUserEntity user = this.loadUserByUserId(userId);

        if (this.getPasswordEncoder().matches(password.trim(), user.getPassword())) {
            // Password matches successful, do nothing.
        }

        throw new BadCredentialsException("Bad Credentials");
    }

    default void updatePassword(String userId, String newPassword) throws UserNotFoundException {
        UserDataValidator.validPassword(newPassword);
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setPassword(this.getPasswordEncoder().encode(newPassword));
        this.updateUser(user);
    }

    default void updatePassword(String userId, String oldPassword, String newPassword) throws UserNotFoundException {
        this.checkPassword(userId, oldPassword);
        UserDataValidator.validPassword(newPassword);
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setPassword(this.getPasswordEncoder().encode(newPassword.trim()));
        this.updateUser(user);
    }
}
