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

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import org.eulerframework.common.util.StringUtils;
import org.eulerframework.web.module.authentication.conf.SecurityConfig;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.entity.EulerUserProfileEntity;
import org.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import org.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * @author cFrost
 *
 */
@Transactional
public abstract class UserRegistService {
    private final static String[] INTERESTING_NAMES = {"folgandros", "arild", "getaria", "norcia", "lavenham", "bolgheri"};
    
    public abstract EulerUserEntityService getEulerUserEntityService();
    public abstract List<EulerUserProfileService<? extends EulerUserProfileEntity>> getEulerUserProfileServices();
    public abstract List<EulerUserExtraDataProcessor> getEulerUserExtraDataProcessors();

    private Random random = new Random();
    
    protected abstract EulerUserEntity doSignup(String username, String email, String mobile, String password) throws UserInfoCheckWebException;
    
    /**
     * 注册新用户，用户名、邮箱、手机号必须有一个有值，当用户名为空时，会随机一个格式为“随机名字_手机号或邮箱”的用户名
     * @param username 用户名
     * @param email 注册邮箱
     * @param mobile 注册手机号
     * @param password 密码
     * @return 注册生成的用户实体
     */
    public EulerUserEntity signUp(String username, String email, String mobile, String password) 
            throws UserInfoCheckWebException {
        
        if(StringUtils.isEmpty(username) && (StringUtils.hasText(email) || StringUtils.hasText(mobile))) {
            username = this.randomUsername();
        }

        UserDataValidator.validUsername(username);
        username = username.trim();
        
        if(StringUtils.hasText(email)) {
            UserDataValidator.validEmail(email);
            email = email.trim();
        } else {
            email = null;
        }
        
        if(StringUtils.hasText(mobile)) {
            UserDataValidator.validMobile(mobile);
            mobile = mobile.trim();
        } else {
            mobile = null;
        }
        
        UserDataValidator.validPassword(password);
        password = password.trim();
        
        return this.doSignup(username, email, mobile, password);
    }
    
    private String randomUsername() {
        String username = UUID.randomUUID().toString().substring(0, 8) + UUID.randomUUID().toString().substring(14, 18);
        
        if(SecurityConfig.isEnableInterestingRandomUsernamePrefix()) {
            String perfix = INTERESTING_NAMES[random.nextInt(INTERESTING_NAMES.length)];
            return perfix + "_" + username;
        }
        
        return username;
    }
    
    /**
     * 注册新用户
     * @param username 用户名
     * @param email 注册邮箱
     * @param mobile 注册手机号
     * @param password 密码
     * @param userProfile 用户档案
     * @return 注册生成的用户实体
     */
    public EulerUserEntity signUp(String username, String email, String mobile, String password, EulerUserProfileEntity userProfile) 
            throws UserInfoCheckWebException {
        Assert.notNull(getEulerUserProfileServices(), "At least one EulerUserProfileServices should be implemented");
        EulerUserEntity user = this.signUp(username, email, mobile, password);
        userProfile.setUserId(user.getUserId());
        for(EulerUserProfileService<? extends EulerUserProfileEntity> eulerUserProfileService : getEulerUserProfileServices()) {
            if(eulerUserProfileService.isMyProfile(userProfile)) {
                eulerUserProfileService.createUserProfile(userProfile);
            }
        }
        return user;
    }
    
    /**
     * 注册新用户, 并尝试处理附加数据, 
     * 当找到第一个可以处理附加数据的{@link EulerUserExtraDataProcessor}后, 就会使用此处理器处理完毕并返回结果,
     * 如果当前应用上下文中没有{@link EulerUserExtraDataProcessor}的实现类或者所有实现类均无法处理传入的附加数据,
     * 方法不会报错, 仍会完成基本的用户注册流程, 并返回结果.
     * <br>
     * <b>注意: </b>因为只有第一个符合条件的附加数据处理器处理器可以生效, 所以应尽量避免应用上下文中存在多个可以处理同一类附加数据的处理器.
     * 
     * @param username 用户名
     * @param email 注册邮箱
     * @param mobile 注册手机号
     * @param password 密码
     * @param extraData 附加数据
     * @return 注册生成的用户实体
     */
    public EulerUserEntity signUp(String username, String email, String mobile, String password, Map<String, Object> extraData) 
            throws UserInfoCheckWebException {
        EulerUserEntity user = this.signUp(username, email, mobile, password);
        if(extraData != null && !extraData.isEmpty() && getEulerUserExtraDataProcessors() != null) {
            for(EulerUserExtraDataProcessor eulerUserExtraDataProcessor : getEulerUserExtraDataProcessors()) {
                if(eulerUserExtraDataProcessor.process(user.getUserId(), extraData)) {
                    break;
                }
            }
        }
        return user;
    }

}
