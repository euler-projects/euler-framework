package net.eulerform.web.module.authentication.service;

import java.io.Serializable;
import java.util.List;

import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.module.authentication.entity.User;

public interface IUserService extends IBaseService {

    public void createUser(String username, String password);

    public List<User> findAllUsers();

    public User findUserById(Serializable id);

    public List<User> findUserByNameOrCode(String nameOrCode);
}
