package net.eulerform.web.core.security.authentication.dao.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import net.eulerform.web.core.base.dao.hibernate5.impl.BaseDao;
import net.eulerform.web.core.security.authentication.dao.IClientDao;
import net.eulerform.web.core.security.authentication.entity.Client;

public class ClientDao extends BaseDao<Client> implements IClientDao {

    @Override
    public Client findClientByClientId(String clientId) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(super.entityClass);
        detachedCriteria.add(Restrictions.eq("clientId", clientId));
        List<Client> clients = this.findBy(detachedCriteria);
        if(clients == null || clients.isEmpty()) return null;        
        return clients.get(0);
    }

}
