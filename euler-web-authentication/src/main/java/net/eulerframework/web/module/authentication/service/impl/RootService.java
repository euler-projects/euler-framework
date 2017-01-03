package net.eulerframework.web.module.authentication.service.impl;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.eulerframework.common.util.FileReader;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.core.exception.NotFoundException;
import net.eulerframework.web.module.authentication.dao.IUserDao;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.IRootService;

@Service
public class RootService extends BaseService implements IRootService {

    @Resource private IUserDao userDao;
    @Resource private PasswordEncoder passwordEncoder;
    
    @Override
    public void resetRootPasswordRWT() {
        User root = this.userDao.findUserByName("root");
        if(!root.getPassword().equals("NaN"))
            throw new NotFoundException();
        
        String newPassword = UUID.randomUUID().toString();
        
        root.setPassword(passwordEncoder.encode(newPassword));
        
        String webInfPath = this.getServletContext().getRealPath("/WEB-INF");
        String rootpasswordFilePath = webInfPath + "/.rootpassword";
        
        try {
            FileReader.writeFile(rootpasswordFilePath, newPassword, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.userDao.update(root);
        
        this.userDao.flushSession();
        
        System.out.println("root password has been reset, new password was saved in " + rootpasswordFilePath);
    }

    @Override
    public void resetAdminPasswordRWT() {
        User admin = this.userDao.findUserByName("admin");
        if(!admin.getPassword().equals("NaN"))
            throw new NotFoundException();
        
        String newPassword = UUID.randomUUID().toString();
        
        admin.setPassword(passwordEncoder.encode(newPassword));
        
        String webInfPath = this.getServletContext().getRealPath("/WEB-INF");
        String adminpasswordFilePath = webInfPath + "/.adminpassword";
        
        try {
            FileReader.writeFile(adminpasswordFilePath, newPassword, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        this.userDao.update(admin);
        
        this.userDao.flushSession();
        
        System.out.println("admin password has been reset, new password was saved in " + adminpasswordFilePath);

    }

}
