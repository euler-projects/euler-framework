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

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.BasicUserProfileDao;
import net.eulerframework.web.module.authentication.entity.BasicUserProfile;
import net.eulerframework.web.module.authentication.entity.EulerUserProfileEntity;

/**
 * @author cFrost
 *
 */
@Service
public class BasicUserProfileService extends BaseService implements EulerUserProfileService {
    
    @Resource private BasicUserProfileDao basicUserProfileDao;

    @Override
    public boolean isMyProfile(EulerUserProfileEntity userProfile) {
        Assert.notNull(userProfile, "userProfile can not be null");
        return BasicUserProfile.class.equals(userProfile.getClass());
    }

    @Override
    public BasicUserProfile createUserProfile(EulerUserProfileEntity userProfile) {
        BasicUserProfile basicUserProfile = (BasicUserProfile) userProfile;
        this.basicUserProfileDao.save(basicUserProfile);
        return basicUserProfile;
    }

    @Override
    public <T extends EulerUserProfileEntity> T loadUserProfile(String userId, Class<? extends T> profileClass) {
        // TODO Auto-generated method stub
        return null;
    }

}
