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
package net.eulerframework.web.module.authentication.util;

import org.springframework.util.StringUtils;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.EulerUserEntityService;

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
