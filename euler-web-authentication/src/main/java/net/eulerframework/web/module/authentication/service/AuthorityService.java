package net.eulerframework.web.module.authentication.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.eulerframework.common.util.BeanTool;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.IAuthorityDao;
import net.eulerframework.web.module.authentication.dao.IGroupDao;
import net.eulerframework.web.module.authentication.entity.Authority;
import net.eulerframework.web.module.authentication.entity.Group;

public class AuthorityService extends BaseService {

    private IGroupDao groupDao;
    private IAuthorityDao authorityDao;

    public void setGroupDao(IGroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setAuthorityDao(IAuthorityDao authorityDao) {
        this.authorityDao = authorityDao;
    }

    public PageResponse<Group> findGroupByPage(PageQueryRequest pageQueryRequest) {
        return this.groupDao.findGroupByPage(pageQueryRequest);
    }

    public PageResponse<Authority> findAuthorityByPage(PageQueryRequest pageQueryRequest) {
        return this.authorityDao.findAuthorityByPage(pageQueryRequest);
    }

    public List<Group> findAllGroups() {
        List<Group> result = this.groupDao.findAllGroupsInOrder();
        for (Group data : result) {
            data.setAuthorities(null);
        }
        return result;
    }

    public List<Authority> findAllAuthorities() {
        return this.authorityDao.findAllAuthoritiesInOrder();
    }

    public List<Group> findGroupByIds(String[] idArray) {
        return this.groupDao.load(idArray);
    }

    public List<Authority> findAuthorityByIds(String[] idArray) {
        return this.authorityDao.load(idArray);
    }

    public void saveGroup(Group group) {
        BeanTool.clearEmptyProperty(group);
        if (group.getId() != null) {
            Group tmp = null;
            if (group.getAuthorities() == null || group.getAuthorities().isEmpty()) {
                if (tmp == null) {
                    tmp = this.groupDao.load(group.getId());
                }
                group.setAuthorities(tmp.getAuthorities());
            }
        }
        this.groupDao.saveOrUpdate(group);
    }

    public void saveAuthority(Authority authority) {
        this.authorityDao.saveOrUpdate(authority);
    }

    public void saveGroupAuthorities(String groupId, List<Authority> authorities) {
        Group group = this.groupDao.load(groupId);
        if (group == null)
            throw new RuntimeException("指定的Group不存在");
        Set<Authority> authoritySet = new HashSet<>(authorities);
        group.setAuthorities(authoritySet);
        this.groupDao.saveOrUpdate(group);
    }

    public void deleteGroups(String[] idArray) {
        this.groupDao.deleteByIds(idArray);
    }

    public void deleteAuthorities(String[] idArray) {
        this.authorityDao.deleteByIds(idArray);
    }
}
