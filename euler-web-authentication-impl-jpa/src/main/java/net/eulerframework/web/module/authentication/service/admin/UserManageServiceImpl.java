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
package net.eulerframework.web.module.authentication.service.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.repository.UserRepository;
import net.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * @author cFrost
 *
 */
@Service
public class UserManageServiceImpl extends BaseService implements UserManageService {

    @Resource
    private UserRepository userRepository;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public PageResponse<User> findUserByPage(PageQueryRequest pageQueryRequest) {
        Pageable pageable = PageRequest.of(pageQueryRequest.getPageIndex(), pageQueryRequest.getPageSize());
        
        Specification<User> spec = new Specification<User>() {

            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                // TODO Auto-generated method stub
                String username = pageQueryRequest.getQueryValue("username");
                List<Predicate> p = new ArrayList<>();
                if(StringUtils.hasText(username))  {
                    Predicate a = criteriaBuilder.like(criteriaBuilder.upper(root.get("username")), "%" + username.toUpperCase() + "%");
                    p.add(a);
                }
                String email = pageQueryRequest.getQueryValue("email");
                if(StringUtils.hasText(email))  {
                    Predicate a = criteriaBuilder.like(criteriaBuilder.upper(root.get("email")), "%" + email.toUpperCase() + "%");
                    p.add(a);
                }
                String mobile = pageQueryRequest.getQueryValue("mobile");
                if(StringUtils.hasText(mobile))  {
                    Predicate a = criteriaBuilder.like(criteriaBuilder.upper(root.get("mobile")), "%" + mobile.toUpperCase() + "%");
                    p.add(a);
                }
                if(p.isEmpty()) {
                    return null;
                }
                return criteriaBuilder.or(p.toArray(new Predicate[] {}));
            }
            
        };
        
        Page<User> page = this.userRepository.findAll(spec , pageable);
        //Page<User> page = this.userRepository.findAll(pageable);
        PageResponse<User> ret = new PageResponse<>(
                page.getContent(), 
                page.getTotalElements(), 
                page.getNumber(), 
                page.getSize());
        if (!ret.getRows().isEmpty()) {
            ret.getRows().forEach(user -> user.eraseCredentials());
        }

        return ret;
    }

    @Override
    public void addUser(String username, String email, String mobile, String password, boolean enabled,
            boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired)
            throws UserInfoCheckWebException {
        Assert.hasText(username, "Param 'username' can not be empty");
        Assert.hasText(password, "Param 'password' can not be empty");
        
        User user = new User();
        
        UserDataValidator.validUsername(username);
        user.setUsername(username);
        
        if(StringUtils.hasText(email)) {
            UserDataValidator.validEmail(email);
            user.setEmail(email);
        }
        
        if(StringUtils.hasText(mobile)) {
            UserDataValidator.validMobile(mobile);
            user.setMobile(mobile);
        }
        
        UserDataValidator.validPassword(password);
        user.setPassword(this.passwordEncoder.encode(password));
        user.setEnabled(enabled);
        user.setAccountNonExpired(accountNonExpired);
        user.setAccountNonLocked(accountNonLocked);
        user.setCredentialsNonExpired(credentialsNonExpired);
        user.setRegistTime(new Date());
        
        this.userRepository.save(user);
    }

    @Override
    public void updateUser(String userId, String username, String email, String mobile, boolean enabled,
            boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired)
            throws UserNotFoundException, UserInfoCheckWebException {
        Assert.hasText(userId, "Param 'userId' can not be empty");
        Assert.hasText(username, "Param 'username' can not be empty");
        
        User user = this.userRepository.findUserById(userId);
        
        if(user == null) {
            throw new UserNotFoundException();
        }
        
        if(!user.getUsername().equalsIgnoreCase(username)) {
            UserDataValidator.validUsername(username);            
        }
        user.setUsername(username);

        if(StringUtils.hasText(email) && !email.equalsIgnoreCase(user.getEmail())) {
            UserDataValidator.validEmail(email);       
        }
        user.setEmail(email);
        
        if(StringUtils.hasText(mobile) && !mobile.equalsIgnoreCase(user.getMobile())) {
            UserDataValidator.validMobile(mobile);    
        }
        user.setMobile(mobile);
        
        user.setEnabled(enabled);
        user.setAccountNonExpired(accountNonExpired);
        user.setAccountNonLocked(accountNonLocked);
        user.setCredentialsNonExpired(credentialsNonExpired);
        
        this.userRepository.save(user);
    }

    @Override
    public void updatePassword(String userId, String password) throws UserNotFoundException, UserInfoCheckWebException {
        Assert.hasText(userId, "Param 'userId' can not be empty");
        Assert.hasText(password, "Param 'password' can not be empty");
        
        User user = this.userRepository.findUserById(userId);
        
        if(user == null) {
            throw new UserNotFoundException();
        }
        
        UserDataValidator.validPassword(password);
        user.setPassword(this.passwordEncoder.encode(password));

        this.userRepository.save(user);
    }

    @Override
    @Transactional
    public void activeUser(String userId) throws UserNotFoundException {
        Assert.hasText(userId, "Param 'userId' can not be empty");
        
        User user = this.userRepository.findUserById(userId);
        
        if(user == null) {
            throw new UserNotFoundException();
        }

        user.setEnabled(true);
        
        this.userRepository.save(user);
    }

    @Override
    @Transactional
    public void blockUser(String userId) throws UserNotFoundException {
        Assert.hasText(userId, "Param 'userId' can not be empty");
        
        User user = this.userRepository.findUserById(userId);
        
        if(user == null) {
            throw new UserNotFoundException();
        }

        user.setEnabled(false);
        
        this.userRepository.save(user);
    }

}
