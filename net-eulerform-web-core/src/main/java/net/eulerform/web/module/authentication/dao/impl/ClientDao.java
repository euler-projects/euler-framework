package net.eulerform.web.module.authentication.dao.impl;

import java.util.List;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.module.authentication.dao.IClientDao;
import net.eulerform.web.module.authentication.entity.Client;

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

}
