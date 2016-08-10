package net.eulerform.web.module.authentication.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.module.authentication.entity.Group;

public interface IGroupDao extends IBaseDao<Group> {

    PageResponse<Group> findGroupByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    List<Group> findAllGroupsInOrder();

    Group findSystemUsersGroup();

}
