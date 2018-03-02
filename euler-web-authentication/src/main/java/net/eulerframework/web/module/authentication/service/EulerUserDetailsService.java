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

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.web.module.authentication.conf.SecurityConfig;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.principal.EulerUserDetails;

/**
 * @author cFrost
 *
 */
@Service("userDetailsService")
public class EulerUserDetailsService implements UserDetailsService {
    
    private final static DefaultObjectCache<String, EulerUserDetails> USER_CAHCE 
    = ObjectCachePool.generateDefaultObjectCache(SecurityConfig.getUserDetailsCacheLife());

    private boolean userDetailsCacheEnabled = SecurityConfig.isEnableUserDetailsCache();
    private boolean enableEmailSignin = SecurityConfig.isEnableMobileSignin();
    private boolean enableMobileSignin = SecurityConfig.isEnableMobileSignin();

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
            } catch (UserNotFoundException e1) {
                if (this.enableEmailSignin) {
                    try {
                        return this.eulerUserEntityService.loadUserByEmail(username).toEulerUserDetails();
                    } catch (UserNotFoundException e2) {
                        if (this.enableMobileSignin) {
                            try {
                                return this.eulerUserEntityService.loadUserByMobile(username).toEulerUserDetails();
                            } catch (UserNotFoundException e3) {
                                throw e3;
                            }
                        } else {
                            throw e2;
                        }
                    }
                } else {
                    throw e1;
                }
            }
        } catch (UserNotFoundException e) {
            return null;
        }
    }
}
