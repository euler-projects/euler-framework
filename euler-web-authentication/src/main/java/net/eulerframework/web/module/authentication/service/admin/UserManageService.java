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
package net.eulerframework.web.module.authentication.service.admin;

import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;

/**
 * @author cFrost
 *
 */
public interface UserManageService extends IBaseService {

    PageResponse<? extends EulerUserEntity> findUserByPage(PageQueryRequest pageQueryRequest);

    /**
     * 后台管理 - 创建新用户
     * @param username 用户名
     * @param email 用户E-Mail
     * @param mobile 用户手机号
     * @param password 密码
     * @param enabled 是否已启用
     * @param accountNonExpired 账号永不过期
     * @param accountNonLocked 账号未锁定
     * @param credentialsNonExpired 密码永不过期
     * @throws UserInfoCheckWebException 当用户名、E-Mail、手机号、密码校验不符合要求时抛出此异常
     */
    void addUser(String username, String email, String mobile, String password, boolean enabled,
            boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired)
            throws UserInfoCheckWebException;

    /**
     * 后台管理 - 修改用户信息
     * @param userId 被修改的用户ID
     * @param username 修改后的用户名
     * @param email 修改后的E-Mail
     * @param mobile 修改后的手机号
     * @param enabled 是否已启用
     * @param accountNonExpired 账号永不过期
     * @param accountNonLocked 账号未锁定
     * @param credentialsNonExpired 密码永不过期
     * @throws UserNotFoundException 当被修改的用户ID不存在时抛出此异常
     * @throws UserInfoCheckWebException 当用户名、E-Mail、手机号校验不符合要求时抛出此异常
     */
    void updateUser(String userId, String username, String email, String mobile, boolean enabled,
            boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired)
            throws UserNotFoundException, UserInfoCheckWebException;

    /**
     * 后台管理 - 重置密码
     * @param userId 待重置密码的用户ID
     * @param password 新密码
     * @throws UserNotFoundException 当用户ID不存在时抛出此异常
     * @throws UserInfoCheckWebException 当密码校验不符合要求时抛出此异常
     */
    void updatePassword(String userId, String password) throws UserNotFoundException, UserInfoCheckWebException;

    /**
     * 后台管理 - 启用用户
     * @param userId 待启用的用户ID
     * @throws UserNotFoundException 当用户ID不存在时抛出此异常
     */
    void activeUser(String userId) throws UserNotFoundException;

    /**
     * 后台管理 - 禁用用户
     * @param userId 待禁用的用户ID
     * @throws UserNotFoundException 当用户ID不存在时抛出此异常
     */
    void blockUser(String userId) throws UserNotFoundException;

}
