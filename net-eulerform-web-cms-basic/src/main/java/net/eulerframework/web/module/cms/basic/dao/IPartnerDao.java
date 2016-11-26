package net.eulerframework.web.module.cms.basic.dao;

import java.util.List;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.cms.basic.entity.Partner;

public interface IPartnerDao extends IBaseDao<Partner> {

    public int findMaxOrder();

    public PageResponse<Partner> findPartnerByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    public List<Partner> loadPartners(boolean onlyShow);

    public List<Partner> findPartnerByNameFuzzy(String name);

}
