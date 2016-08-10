package net.eulerform.web.module.authentication.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.module.authentication.entity.Group;
import net.eulerform.web.module.authentication.entity.User;

public interface IUserService extends IBaseService {

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public PageResponse<User> findUserByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void saveUser(User user);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void saveUserGroups(String userId, List<Group> groups);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void deleteUsers(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void enableUsersRWT(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void disableUsersRWT(String[] idArray);
    
    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    public void resetUserPasswordRWT(String userId, String newPassword);

    public User findUserById(Serializable id);

    public List<User> findUserByNameOrCode(String nameOrCode);

    public void createUser(String username, String password);

}
