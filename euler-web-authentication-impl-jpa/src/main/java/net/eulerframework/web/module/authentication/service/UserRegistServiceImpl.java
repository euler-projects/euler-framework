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
