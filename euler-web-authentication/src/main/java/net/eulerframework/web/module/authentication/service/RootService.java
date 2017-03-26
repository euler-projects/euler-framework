package net.eulerframework.web.module.authentication.service;

import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.common.util.io.file.SimpleFileIOUtils;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.core.exception.ResourceNotFoundException;
import net.eulerframework.web.module.authentication.dao.UserDao;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.util.ServletUtils;

@Service
@Transactional
public class RootService extends BaseService {

    @Resource private UserDao userDao;
    @Resource private PasswordEncoder passwordEncoder;

    /**
     * 重置root用户的密码,只用root用户在数据的密码字段被设置为NaN才能使用,重置的密码保存在WEB-INF下的.rootpassword文件中
     */
    public void resetRootPassword() {
        User root = this.userDao.findUserByName("root");
        if(!root.getPassword().equals("NaN"))
            throw new ResourceNotFoundException();
        
        String newPassword = StringUtils.randomString(16);
        
        root.setPassword(passwordEncoder.encode(newPassword));
        
        String webInfPath = ServletUtils.getServletContext().getRealPath("/WEB-INF");
        String rootpasswordFilePath = webInfPath + "/.rootpassword";
        
        try {
            SimpleFileIOUtils.writeFile(rootpasswordFilePath, newPassword, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.userDao.update(root);
        
        this.userDao.flushSession();
        
        System.out.println("root password has been reset, new password was saved in " + rootpasswordFilePath);
    }

    /**
     * 重置admin用户的密码,只用admin用户在数据的密码字段被设置为NaN才能使用,重置的密码保存在WEB-INF下的.adminpassword文件中
     */
    public void resetAdminPassword() {
        User admin = this.userDao.findUserByName("admin");
        if(!admin.getPassword().equals("NaN"))
            throw new ResourceNotFoundException();

        String newPassword = StringUtils.randomString(16);
        
        admin.setPassword(passwordEncoder.encode(newPassword));
        
        String webInfPath = ServletUtils.getServletContext().getRealPath("/WEB-INF");
        String adminpasswordFilePath = webInfPath + "/.adminpassword";
        
        try {
            SimpleFileIOUtils.writeFile(adminpasswordFilePath, newPassword, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        this.userDao.update(admin);
        
        this.userDao.flushSession();
        
        System.out.println("admin password has been reset, new password was saved in " + adminpasswordFilePath);

    }

}
