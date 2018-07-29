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
 */
package net.eulerframework.web.module.authentication.service;

import java.util.List;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.eulerframework.web.module.authentication.entity.EulerUserProfileEntity;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;

/**
 * @author cFrost
 *
 */
@Transactional
@Service("userRegistService")
public class UserRegistServiceImpl extends UserRegistService {
    @Resource 
    private EulerUserEntityService eulerUserEntityService;
    @Resource 
    private PasswordEncoder passwordEncoder;
    @Autowired(required = false) 
    private List<EulerUserProfileService<? extends EulerUserProfileEntity>> eulerUserProfileServices;
    @Autowired(required = false) 
    private List<EulerUserExtraDataProcessor> eulerUserExtraDataProcessors;

    @Override
    public EulerUserEntityService getEulerUserEntityService() {
        return this.eulerUserEntityService;
    }

    @Override
    public List<EulerUserProfileService<? extends EulerUserProfileEntity>> getEulerUserProfileServices() {
        return this.eulerUserProfileServices;
    }

    @Override
    public List<EulerUserExtraDataProcessor> getEulerUserExtraDataProcessors() {
        return this.eulerUserExtraDataProcessors;
    }

    @Override
    protected User doSignup(String username, String email, String mobile, String password)
            throws UserInfoCheckWebException {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setPassword(this.passwordEncoder.encode(password.trim()));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        return (User) this.eulerUserEntityService.createUser(user);
    }

}
