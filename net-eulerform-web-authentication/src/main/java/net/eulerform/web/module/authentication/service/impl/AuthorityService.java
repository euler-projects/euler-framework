package net.eulerform.web.module.authentication.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.eulerform.common.BeanTool;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.module.authentication.dao.IAuthorityDao;
import net.eulerform.web.module.authentication.dao.IGroupDao;
import net.eulerform.web.module.authentication.entity.Authority;
import net.eulerform.web.module.authentication.entity.Group;
import net.eulerform.web.module.authentication.service.IAuthorityService;

public class AuthorityService extends BaseService implements IAuthorityService {
    
    private IGroupDao groupDao;
    private IAuthorityDao authorityDao;

    public void setGroupDao(IGroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setAuthorityDao(IAuthorityDao authorityDao) {
        this.authorityDao = authorityDao;
    }

    @Override
    public PageResponse<Group> findGroupByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.groupDao.findGroupByPage(queryRequest, pageIndex, pageSize);
    }

    @Override
    public PageResponse<Authority> findAuthorityByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.authorityDao.findAuthorityByPage(queryRequest, pageIndex, pageSize);
    }

    @Override
    public List<Group> findAllGroups() {
        List<Group> result = this.groupDao.findAllGroupsInOrder();
        for(Group data : result) {
            data.setAuthorities(null);
        }
        return result;
    }

    @Override
    public List<Authority> findAllAuthorities() {
        return this.authorityDao.findAllAuthoritiesInOrder();
    }

    @Override
    public List<Group> findGroupByIds(String[] idArray) {
        return this.groupDao.load(idArray);
    }

    @Override
    public List<Authority> findAuthorityByIds(String[] idArray) {
        return this.authorityDao.load(idArray);
    }

    @Override
    public void saveGroup(Group group) {
        BeanTool.clearEmptyProperty(group);
        if(group.getId() != null) {
            Group tmp = null;
            if(group.getAuthorities() == null || group.getAuthorities().isEmpty()){
                if(tmp == null) {
                    tmp = this.groupDao.load(group.getId());
                }
                group.setAuthorities(tmp.getAuthorities());
            }
        }
        this.groupDao.saveOrUpdate(group);
    }

    @Override
    public void saveAuthority(Authority authority) {
        this.authorityDao.saveOrUpdate(authority);
    }

    @Override
    public void saveGroupAuthorities(String groupId, List<Authority> authorities) {
        Group group = this.groupDao.load(groupId);
        if(group == null)
            throw new RuntimeException("指定的Group不存在");
        Set<Authority> authoritySet = new HashSet<>(authorities);
        group.setAuthorities(authoritySet);
        this.groupDao.saveOrUpdate(group);
    }   

    @Override
    public void deleteGroups(String[] idArray) {
        this.groupDao.deleteByIds(idArray);
    }

    @Override
    public void deleteAuthorities(String[] idArray) {
        this.authorityDao.deleteByIds(idArray);
    }
}
