package net.eulerform.web.module.oauth2.dao.impl;

import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerform.common.util.StringTool;
import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.oauth2.dao.IClientDao;
import net.eulerform.web.module.oauth2.entity.Client;

public class ClientDao extends BaseDao<Client> implements IClientDao {

    @Override
    public Client findClientByClientId(String clientId) {
        
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.add(Restrictions.eq("clientId", clientId));
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        List<Client> clients = this.findBy(detachedCriteria);
        if (clients == null || clients.isEmpty())
            return null;
        Client client = clients.get(0);
        return client;
    }

    @Override
    public PageResponse<Client> findClientByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass)
                .setFetchMode("resources", FetchMode.SELECT)
                .setFetchMode("scopes", FetchMode.SELECT)
                .setFetchMode("authorizedGrantTypes", FetchMode.SELECT)
                .setFetchMode("registeredRedirectUri", FetchMode.SELECT);
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("clientId");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("clientId", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("description");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("description", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("neverNeedApprove");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.eq("neverNeedApprove", Boolean.parseBoolean(queryValue)));
            }
            queryValue = queryRequest.getQueryValue("enabled");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.eq("enabled", Boolean.parseBoolean(queryValue)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("clientId"));
        
        PageResponse<Client> result = this.findPageBy(detachedCriteria, pageIndex, pageSize);
        
        List<Client> clients = result.getRows();
        for(Client data : clients) {
            data.setClientSecret(null);
        }
        return result;
    }

}
