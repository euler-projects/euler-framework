package net.eulerform.web.core.security.authentication.service;

import java.util.List;

import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.core.security.authentication.entity.User;

public interface IUserService extends IBaseService {

    public void createUser(String username, String password);

    public List<User> findAllUsers(boolean loadRoles);
}
