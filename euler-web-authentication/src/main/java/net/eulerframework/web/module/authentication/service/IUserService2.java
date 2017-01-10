package net.eulerframework.web.module.authentication.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.User;

public interface IUserService2 extends IBaseService {

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('AUTH_ADMIN','ADMIN','ROOT')")
    public PageResponse<User> findUserByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('AUTH_ADMIN','ADMIN','ROOT')")
    public void saveUser(User user);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('AUTH_ADMIN','ADMIN','ROOT')")
    public void saveUserGroups(String userId, List<Group> groups);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('AUTH_ADMIN','ADMIN','ROOT')")
    public void deleteUsers(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('AUTH_ADMIN','ADMIN','ROOT')")
    public void enableUsersRWT(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('AUTH_ADMIN','ADMIN','ROOT')")
    public void disableUsersRWT(String[] idArray);

    public User findUserById(Serializable id);

    public List<User> findUserByNameOrCode(String nameOrCode);
}
