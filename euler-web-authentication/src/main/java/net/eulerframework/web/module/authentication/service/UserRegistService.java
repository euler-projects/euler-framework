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

import java.util.List;

import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.entity.EulerUserProfileEntity;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;

/**
 * @author cFrost
 *
 */
public interface UserRegistService {
    
    EulerUserEntityService getEulerUserEntityService();
    List<EulerUserProfileService> getEulerUserProfileServices();

    /**
     * 注册新用户
     * @param username 用户名
     * @param email 注册邮箱
     * @param mobile 注册手机号
     * @param password 密码
     * @return 注册生成的用户实体
     */
    EulerUserEntity signUp(String username, String email, String mobile, String password) 
            throws UserInfoCheckWebException;

    /**
     * 注册新用户
     * @param username 用户名
     * @param email 注册邮箱
     * @param mobile 注册手机号
     * @param password 密码
     * @param userProfile 用户档案
     */
    default void signUp(String username, String email, String mobile, String password, EulerUserProfileEntity userProfile) 
            throws UserInfoCheckWebException {
        EulerUserEntity user = this.signUp(username, email, mobile, password);
        userProfile.setUserId(user.getUserId());
        for(EulerUserProfileService eulerUserProfileService : getEulerUserProfileServices()) {
            if(eulerUserProfileService.isMyProfile(userProfile)) {
                eulerUserProfileService.createUserProfile(userProfile);
            }
        }
    }

}
