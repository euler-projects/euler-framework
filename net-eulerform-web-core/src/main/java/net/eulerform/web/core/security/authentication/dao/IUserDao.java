package net.eulerform.web.core.security.authentication.dao;

import net.eulerform.web.core.base.dao.hibernate5.IBaseDao;
import net.eulerform.web.core.security.authentication.entity.User;

public interface IUserDao extends IBaseDao<User> {

    User findUserByName(String username);

}
