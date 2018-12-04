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

import org.eulerframework.common.util.JavaObjectUtils;
import org.eulerframework.web.module.authentication.entity.EulerUserProfileEntity;

/**
 * @author cFrost
 *
 */
public interface EulerUserProfileService<T extends EulerUserProfileEntity> {

    /**
     * 判断一个用户档案实体类是否可由此实现类处理
     * 
     * @param userProfile 用户档案
     * @return <code>true</code>表示时自己可以处理的用户档案
     */
    default boolean isMyProfile(EulerUserProfileEntity userProfile) {
        return userProfile.getClass().equals(JavaObjectUtils.findSuperInterfaceGenricType(this.getClass(), 0, 0));
    }

    /**
     * 创建用户档案
     * 
     * @param userProfile 用户档案
     * @return 新创建的用户档案实体，与传入参数是同一个实例
     */
    EulerUserProfileEntity createUserProfile(EulerUserProfileEntity userProfile);
    
    T loadUserProfile(String userId);

}
