package net.eulerframework.web.module.authentication.dao;

import java.util.List;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.authentication.entity.Group;

public interface IGroupDao extends IBaseDao<Group> {

    PageResponse<Group> findGroupByPage(PageQueryRequest pageQueryRequest);

    List<Group> findAllGroupsInOrder();

    Group findSystemUsersGroup();

}
