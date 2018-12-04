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
package org.eulerframework.web.module.authentication.util;

import org.springframework.util.StringUtils;

import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import org.eulerframework.cache.inMemoryCache.ObjectCachePool;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.service.EulerUserEntityService;

/**
 * @author cFrost
 *
 */
public abstract class SecurityTag {

    private static final DefaultObjectCache<String, EulerUserEntity> USER_ID_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(60_000);

    private static EulerUserEntityService eulerUserEntityService;
    
    public static void setEulerUserEntityService(EulerUserEntityService eulerUserEntityService) {
        SecurityTag.eulerUserEntityService = eulerUserEntityService;
    }
    
    public static String userIdtoUserame(String userId) {
        if(!StringUtils.hasText(userId)) {
            return "-";
        }
        
        EulerUserEntity user = USER_ID_CAHCE.get(userId, key -> {
            try {
                return eulerUserEntityService.loadUserByUserId(userId);
            } catch (UserNotFoundException e) {
                return null;
            }
        });
        
        return user == null ? "-" : user.getUsername();
    }

}
