package net.eulerframework.web.module.basic.dao;

import java.util.List;
import java.util.Locale;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.module.basic.entity.Dictionary;

public class DictionaryDao extends BaseDao<Dictionary> {

    /**
     * @param key
     * @return
     */
    public Dictionary findDictionaryByKey(String key, Locale locale) {
        Assert.hasText(key, "key is null");
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.add(Restrictions.eq("key", key));
        detachedCriteria.add(Restrictions.eq("locale", locale));
        List<Dictionary> ret = this.query(detachedCriteria);
        
        return CollectionUtils.isEmpty(ret) ? null : ret.get(0);
    }
    
}
