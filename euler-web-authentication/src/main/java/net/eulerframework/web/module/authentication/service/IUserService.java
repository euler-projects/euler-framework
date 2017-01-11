package net.eulerframework.web.module.authentication.service;

import java.util.List;

import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;

public interface IUserService extends IBaseService {

    public User loadUserByUsername(String username);
    
    public User loadUserByEmail(String email);
    
    public User loadUserByMobile(String mobile);

    List<User> loadUserByNameOrCodeFuzzy(String nameOrCode);

    User loadUser(String userId);

    public String save(User user);

    public PageResponse<User> findUserByPage(PageQueryRequest pageQueryRequest);

    /**
     * 更新用户信息,注意用户密码，用户权限，用户组等的处理，如不指定这些字段，原有字段会被删掉。
     * @param user
     * @throws UserNotFoundException
     */
    public void updateUser(User user) throws UserNotFoundException;
}
