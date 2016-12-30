package net.eulerframework.web.module.authentication.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.IUserProfile;
import net.eulerframework.web.module.authentication.entity.User;

public interface IUserService extends IBaseService {

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
    
    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('AUTH_ADMIN','ADMIN','ROOT')")
    public void resetUserPasswordRWT(String userId, String newPassword);

    public User findUserById(Serializable id);

    public List<User> findUserByNameOrCode(String nameOrCode);

    public void createUser(User user);

    public void createUser(User user, IUserProfile userProfile);
    
    public void createUser(String username, String password);

    public User checkResetTokenRT(String userId, String resetToken);
    
    public void resetUserPasswordWithResetTokenRWT(String userId, String newPassword, String resetToken);
    
    public void forgotPasswordRWT(String email);
}
