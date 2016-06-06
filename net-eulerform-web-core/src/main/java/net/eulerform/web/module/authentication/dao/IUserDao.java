package net.eulerform.web.module.authentication.dao;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.module.authentication.entity.User;

public interface IUserDao extends IBaseDao<User> {

    User findUserByName(String username);

}
