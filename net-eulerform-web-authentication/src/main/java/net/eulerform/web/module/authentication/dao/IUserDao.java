package net.eulerform.web.module.authentication.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.authentication.entity.User;

public interface IUserDao extends IBaseDao<User> {

    User findUserByName(String username);

    User findUserByEmail(String email);

    User findUserByMobile(String moile);

    User findUserByResetToken(String resetToken);
    
    List<User> findUserByNameOrCode(String nameOrCode);

    PageResponse<User> findUserByPage(QueryRequest queryRequest, int pageIndex, int pageSize);


}
