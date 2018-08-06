/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 Euler Project 
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
 */
package net.eulerframework.web.module.authentication.service.admin;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

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
        List<User> all = this.userRepository.findAll();
        PageResponse<User> ret = new PageResponse<>(all, all.size(), pageQueryRequest.getPageIndex(), pageQueryRequest.getPageSize());//this.userDao.pageQuery(pageQueryRequest);
        //TODO:实现分页
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
