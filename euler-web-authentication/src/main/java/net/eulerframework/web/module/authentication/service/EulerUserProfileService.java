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

import net.eulerframework.web.module.authentication.entity.EulerUserProfileEntity;

/**
 * @author cFrost
 *
 */
public interface EulerUserProfileService {

    /**
     * 判断一个用户档案实体类是否可由此实现类处理
     * 
     * @param userProfile 用户档案
     * @return <code>true</code>表示时自己可以处理的用户档案
     */
    boolean isMyProfile(EulerUserProfileEntity userProfile);

    /**
     * 创建用户档案
     * 
     * @param userProfile 用户档案
     * @return 新创建的用户档案实体，与传入参数是同一个实例
     */
    EulerUserProfileEntity createUserProfile(EulerUserProfileEntity userProfile);
    
    <T extends EulerUserProfileEntity> T loadUserProfile(String userId, Class<? extends T> profileClass);

}
