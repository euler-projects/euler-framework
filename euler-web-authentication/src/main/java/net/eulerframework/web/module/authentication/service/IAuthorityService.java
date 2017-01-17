package net.eulerframework.web.module.authentication.service;

import java.util.List;

import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.authentication.entity.Authority;
import net.eulerframework.web.module.authentication.entity.Group;

public interface IAuthorityService extends IBaseService {

    public PageResponse<Group> findGroupByPage(PageQueryRequest pageQueryRequest);

    public PageResponse<Authority> findAuthorityByPage(PageQueryRequest pageQueryRequest);

    public List<Group> findAllGroups();

    public List<Authority> findAllAuthorities();

    public List<Group> findGroupByIds(String[] idArray);

    public List<Authority> findAuthorityByIds(String[] idArray);

    public void saveGroup(Group group);

    public void saveAuthority(Authority authority);

    public void saveGroupAuthorities(String groupId, List<Authority> authorities);

    public void deleteGroups(String[] idArray);

    public void deleteAuthorities(String[] idArray);
}
