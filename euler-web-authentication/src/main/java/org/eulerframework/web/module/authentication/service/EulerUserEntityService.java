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

import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * 用户信息相关业务逻辑接口，对用户基础信息（用户名、注册邮箱、注册手机号、启用、禁用等）操作均通过此接口完成
 * @author cFrost
 *
 */
public interface EulerUserEntityService {

    /**
     * 通过用户ID查找用户实体类
     * 
     * @param userId 用户ID
     * @return 用户实体类
     * @throws UserNotFoundException 查找的用户不存在
     */
    EulerUserEntity loadUserByUserId(String userId) throws UserNotFoundException;

    /**
     * 通过用户名查找用户实体类
     * 
     * @param username 用户名
     * @return 用户实体类
     * @throws UserNotFoundException 查找的用户不存在
     */
    EulerUserEntity loadUserByUsername(String username) throws UserNotFoundException;

    /**
     * 通过注册邮箱查找用户实体类
     * 
     * @param email 注册邮箱
     * @return 用户实体类
     * @throws UserNotFoundException 查找的用户不存在
     */
    EulerUserEntity loadUserByEmail(String email) throws UserNotFoundException;

    /**
     * 通过注册手机号查找用户实体类
     * 
     * @param phone 注册手机号
     * @return 用户实体类
     * @throws UserNotFoundException 查找的用户不存在
     */
    EulerUserEntity loadUserByPhone(String phone) throws UserNotFoundException;

    /**
     * 通过用户名、注册邮箱、注册手机号组合查找用户实体类，有一个参数满足要求即返回，如有多个参数满足要求，则按用户名、邮箱、手机号的优先级返回
     * 
     * @param username 用户名
     * @param email 注册邮箱
     * @param phone 注册手机号
     * @return 用户实体类
     * @throws UserNotFoundException 查找的用户不存在
     */
    //EulerUserEntity loadUserCombox(String username, String email, String phone) throws UserNotFoundException;
    
    /**
     * 创建新用户
     * @param eulerUserEntity 用户实体(无用户ID)
     * @return 用户实体(于传入参数的实体时一个对象实例，增加了用户ID)
     */
    EulerUserEntity createUser(EulerUserEntity eulerUserEntity);
    
    /**
     * 更新用户信息
     * @param eulerUserEntity 要更新的用户实体
     */
    void updateUser(EulerUserEntity eulerUserEntity);

    /**
     * 更新用户名
     * @param userId 要更新用户的ID
     * @param newUsername 新用户名
     * @throws UserNotFoundException 用户未找到
     * @throws UserInfoCheckWebException 用户名不符合要求
     */
    default void updateUsername(String userId, String newUsername) throws UserNotFoundException, UserInfoCheckWebException {
        UserDataValidator.validUsername(newUsername);
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setUsername(newUsername.trim());
        this.updateUser(user);
    }

    /**
     * 更新注册邮箱
     * @param userId 要更新用户的ID
     * @param newEmail 新邮箱
     * @throws UserNotFoundException 用户未找到
     * @throws UserInfoCheckWebException 邮箱地址不符合要求
     */
    default void updateEmail(String userId, String newEmail) throws UserNotFoundException, UserInfoCheckWebException {
        UserDataValidator.validEmail(newEmail);
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setEmail(newEmail.trim());
        this.updateUser(user);
    }

    /**
     * 更新注册手机号
     * @param userId 要更新用户的ID
     * @param newPhone 新手机号
     * @throws UserNotFoundException 用户未找到
     * @throws UserInfoCheckWebException 手机号不符合要求
     */
    default void updatePhone(String userId, String newPhone) throws UserNotFoundException, UserInfoCheckWebException {
        UserDataValidator.validPhone(newPhone);
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setPhone(newPhone.trim());
        this.updateUser(user);
    }
    
    /**
     * 禁用用户
     * @param userId 要禁用用户的ID
     * @throws UserNotFoundException 用户未找到
     */
    default void blockUser(String userId) throws UserNotFoundException {
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setEnabled(false);
        this.updateUser(user);
    }

    /**
     * 激活用户
     * @param userId 要激活用户的ID
     * @throws UserNotFoundException 用户未找到
     */
    default void activeUser(String userId) throws UserNotFoundException {
        EulerUserEntity user = this.loadUserByUserId(userId);
        user.setEnabled(true);
        this.updateUser(user);
    }
    
    void addGroup(String userId, String groupCode);
}
