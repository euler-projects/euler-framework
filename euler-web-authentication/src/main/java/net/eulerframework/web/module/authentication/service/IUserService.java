package net.eulerframework.web.module.authentication.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import net.eulerframework.web.module.authentication.entity.User;

public interface IUserService extends UserDetailsService {
    
    public User loadUserByEmail(String email) throws UsernameNotFoundException;
    
    public User loadUserByMobile(String mobile) throws UsernameNotFoundException;

    List<User> loadUserByNameOrCodeFuzzy(String nameOrCode);

    User loadUser(String userId);
}
