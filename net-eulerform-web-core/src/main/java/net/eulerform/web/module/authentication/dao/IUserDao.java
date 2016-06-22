package net.eulerform.web.module.authentication.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.module.authentication.entity.User;

public interface IUserDao extends IBaseDao<User> {

    User findUserByName(String username);

    List<User> findUserByNameOrCode(String nameOrCode);

}
