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


import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import org.eulerframework.cache.inMemoryCache.ObjectCachePool;
import org.eulerframework.web.module.authentication.conf.SecurityConfig;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.principal.EulerUserDetails;

/**
 * @author cFrost
 *
 */
@Service("userDetailsService")
public class EulerUserDetailsService implements UserDetailsService {
    
    private final static DefaultObjectCache<String, EulerUserDetails> USER_CAHCE 
    = ObjectCachePool.generateDefaultObjectCache(SecurityConfig.getUserDetailsCacheLife());

    private boolean userDetailsCacheEnabled = SecurityConfig.isEnableUserDetailsCache();
    private boolean enableEmailSignin = SecurityConfig.isEnablePhoneSignin();
    private boolean enablePhoneSignin = SecurityConfig.isEnablePhoneSignin();

    @Resource
    private EulerUserEntityService eulerUserEntityService;

    /**
     * 通过用户名查找用户主体信息
     * 
     * @param username
     *            用户名
     * @return 用户主体信息
     * @throws UsernameNotFoundException
     *             查找的用户不存在
     */
    @Override
    public EulerUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EulerUserDetails userDetails;
        if(this.userDetailsCacheEnabled) {
            userDetails = USER_CAHCE.get(username, key -> this.loadUserDetails(key));
        } else {
            userDetails = this.loadUserDetails(username);
        }

        if(userDetails == null) {
            throw new UsernameNotFoundException("user '" + username + "' not exists.");
        } else {
            return userDetails;
        }
    }
    
    private EulerUserDetails loadUserDetails(String username) {
        try {
            try {
                return this.eulerUserEntityService.loadUserByUsername(username).toEulerUserDetails();
            } catch (UserNotFoundException usernameNotFoundException) {
                if (this.enableEmailSignin) {
                    try {
                        return this.eulerUserEntityService.loadUserByEmail(username).toEulerUserDetails();
                    } catch (UserNotFoundException emailNotFoundException) {
                        if (this.enablePhoneSignin) {
                            try {
                                return this.eulerUserEntityService.loadUserByPhone(username).toEulerUserDetails();
                            } catch (UserNotFoundException phoneNotFoundException) {
                                throw phoneNotFoundException;
                            }
                        } else {
                            throw emailNotFoundException;
                        }
                    }
                } else {
                    if (this.enablePhoneSignin) {
                        try {
                            return this.eulerUserEntityService.loadUserByPhone(username).toEulerUserDetails();
                        } catch (UserNotFoundException phoneNotFoundException) {
                            throw phoneNotFoundException;
                        }
                    } else {
                        throw usernameNotFoundException;
                    }
                }
            }
        } catch (UserNotFoundException e) {
            return null;
        }
    }
}
