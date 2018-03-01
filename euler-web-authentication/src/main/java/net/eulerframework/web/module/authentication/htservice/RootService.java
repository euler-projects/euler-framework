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
package net.eulerframework.web.module.authentication.htservice;

import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.common.util.io.file.SimpleFileIOUtils;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.exception.PasswordResetErrorException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.principal.EulerUserDetails;
import net.eulerframework.web.util.ServletUtils;

/**
 * root用户和admin用户密码重置业务逻辑类
 * @author cFrost
 *
 */
@Service
@Transactional("htransactionManager")
public class RootService{
    private final static String ADMIN_USERNAME = "admin";
    private final static String NAN_PASSWD = "NaN";
    
    @Resource private EulerUserEntityService eulerUserEntityService;
    @Resource private PasswordEncoder passwordEncoder;

    /**
     * 重置root用户的密码,只用root用户在数据的密码字段被设置为NaN才能使用,重置的密码保存在WEB-INF下的.rootpassword文件中
     * @throws PasswordResetErrorException 无法重置密码
     * @throws UserNotFoundException root用户不存在
     */
    public void resetRootPassword() throws PasswordResetErrorException, UserNotFoundException {
        String webInfPath = ServletUtils.getServletContext().getRealPath("/WEB-INF");
        String rootpasswordFilePath = webInfPath + "/.rootpassword";
        
        this.resetPasswd(EulerUserDetails.ROOT_USERNAME, rootpasswordFilePath);
    }

    /**
     * 重置admin用户的密码,只用admin用户在数据的密码字段被设置为NaN才能使用,重置的密码保存在WEB-INF下的.adminpassword文件中
     * @throws PasswordResetErrorException 无法重置密码
     * @throws UserNotFoundException admin用户不存在
     */
    public void resetAdminPassword() throws PasswordResetErrorException, UserNotFoundException {
        String webInfPath = ServletUtils.getServletContext().getRealPath("/WEB-INF");
        String adminpasswordFilePath = webInfPath + "/.adminpassword";
        
        this.resetPasswd(ADMIN_USERNAME, adminpasswordFilePath);
    }
    
    private void resetPasswd(String username, String passwdFilePath) throws PasswordResetErrorException, UserNotFoundException {
        EulerUserEntity user = this.eulerUserEntityService.loadUserByUsername(username);
        
        if(!NAN_PASSWD.equals(user.getPassword()))
            throw new PasswordResetErrorException();
        
        String newPassword = StringUtils.randomString(16);
        
        user.setPassword(passwordEncoder.encode(newPassword));
        
        try {
            SimpleFileIOUtils.writeFile(passwdFilePath, newPassword + "\n", false);
        } catch (IOException e) {
            throw new PasswordResetErrorException();
        }
        
        this.eulerUserEntityService.updateUser(user);
        
        System.out.println(username + "'s password has been reset, new password was saved in " + passwdFilePath);
    }

}
