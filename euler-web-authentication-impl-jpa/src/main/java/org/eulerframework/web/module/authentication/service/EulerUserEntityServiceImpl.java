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

import java.util.Date;
import java.util.HashSet;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import org.eulerframework.web.core.base.service.impl.BaseService;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.entity.Group;
import org.eulerframework.web.module.authentication.entity.User;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.repository.GroupRepository;
import org.eulerframework.web.module.authentication.repository.UserRepository;

@Service
public class EulerUserEntityServiceImpl extends BaseService implements EulerUserEntityService {

    @Resource
    private UserRepository userRepository;
    @Resource
    private GroupRepository groupRepository;

    @Override
    public User loadUserByUserId(String userId) throws UserNotFoundException {
        return this.userRepository.findUserById(userId);
    }

    @Override
    public User loadUserByUsername(String username) throws UserNotFoundException {
        User result = this.userRepository.findUserByUsernameIgnoreCase(username);
        
        if(result == null) {
            throw new UserNotFoundException();
        }
        
        return result;
    }

    @Override
    public User loadUserByEmail(String email) throws UserNotFoundException {
        User result = this.userRepository.findUserByEmailIgnoreCase(email);
        
        if(result == null) {
            throw new UserNotFoundException();
        }
        
        return result;
    }

    @Override
    public User loadUserByMobile(String mobile) throws UserNotFoundException {
        User result = this.userRepository.findUserByMobileIgnoreCase(mobile);
        
        if(result == null) {
            throw new UserNotFoundException();
        }
        
        return result;
    }

    @Override
    public User createUser(EulerUserEntity eulerUserEntity) {
        Assert.isTrue(User.class.isAssignableFrom(eulerUserEntity.getClass()), 
                "eulerUserEntity must be an instance of org.eulerframework.web.module.authentication.entity.User");
        User user = (User)eulerUserEntity;
        user.setRegistTime(new Date());
        this.userRepository.save(user);
        return user;
    }

    @Override
    public void updateUser(EulerUserEntity eulerUserEntity) {
        Assert.isTrue(User.class.isAssignableFrom(eulerUserEntity.getClass()), 
                "eulerUserEntity must be an instance of org.eulerframework.web.module.authentication.entity.User");
        User user = (User)eulerUserEntity;
        this.userRepository.save(user);
    }

    @Override
    public void addGroup(String userId, String groupCode) {
        User user = this.loadUserByUserId(userId);
        Group group = this.groupRepository.findGroupByCode(groupCode);
        
        if(user.getGroups() == null) {
            user.setGroups(new HashSet<> ());
        }
        
        user.getGroups().add(group);
        
        this.updateUser(user);
    }
}
