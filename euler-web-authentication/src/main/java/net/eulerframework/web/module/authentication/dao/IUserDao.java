package net.eulerframework.web.module.authentication.dao;

import java.util.List;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.authentication.entity.User;

public interface IUserDao extends IBaseDao<User> {

    User findUserByName(String username);

    User findUserByEmail(String email);

    User findUserByMobile(String moile);
    
    List<User> findUserByNameOrCode(String nameOrCode);

    PageResponse<User> findUserByPage(QueryRequest queryRequest, int pageIndex, int pageSize);


}
