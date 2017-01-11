package net.eulerframework.web.module.authentication.service;

import java.util.List;

import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.User;

public interface IUserService extends IBaseService {

    public User loadUserByUsername(String username);
    
    public User loadUserByEmail(String email);
    
    public User loadUserByMobile(String mobile);

    List<User> loadUserByNameOrCodeFuzzy(String nameOrCode);

    User loadUser(String userId);

    public String save(User user);
}
