package net.eulerform.web.module.basedata.dao.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerform.common.CalendarTool;
import net.eulerform.common.StringTool;
import net.eulerform.web.core.base.dao.impl.hibernate5.BaseModifyInfoDao;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.module.authentication.util.UserContext;
import net.eulerform.web.module.basedata.dao.ICodeTableDao;
import net.eulerform.web.module.basedata.entity.CodeTable;

public class CodeTableDao extends BaseModifyInfoDao<CodeTable> implements ICodeTableDao {
    
    private final static int JS_DICT_TYPE = 1;
    private final static int PROPERTY_TYPE = 2;

    @Override
    public List<CodeTable> findAllCodeOrderByName() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.add(Restrictions.eq("codeType", JS_DICT_TYPE));
        detachedCriteria.addOrder(Order.asc("name"));
        detachedCriteria.addOrder(Order.asc("showOrder"));
        return this.findBy(detachedCriteria);
    }

    @Override
    public List<CodeTable> findAllConfig() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.add(Restrictions.eq("codeType", PROPERTY_TYPE));
        List<CodeTable> result = this.findBy(detachedCriteria);
        if(result == null || result.isEmpty()) return null;        
        return  result;
    }

    @Override
    public CodeTable findConfig(String key) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.add(Restrictions.eq("codeType", PROPERTY_TYPE));
        detachedCriteria.add(Restrictions.eq("key", key));
        List<CodeTable> result = this.findBy(detachedCriteria);
        if(result == null || result.isEmpty()) return null;      
        return result.get(0);
    }

    @Override
    public List<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("name");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("name", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("codeType");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.eq("codeType", Integer.parseInt(queryValue)));
            }
            queryValue = queryRequest.getQueryValue("description");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("description", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("value");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("value", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("valueZhCn");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("valueZhCn", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("valueEnUs");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(Restrictions.like("valueEnUs", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("createBy");
            if (!StringTool.isNull(queryValue)) {
                List<Serializable> ids = UserContext.getUserIdByNameOrCode(queryValue);
                detachedCriteria.add(Restrictions.in("createBy", ids));
            }
            queryValue = queryRequest.getQueryValue("modifyBy");
            if (!StringTool.isNull(queryValue)) {
                List<Serializable> ids = UserContext.getUserIdByNameOrCode(queryValue);
                detachedCriteria.add(Restrictions.in("modifyBy", ids));
            }
            queryValue = queryRequest.getQueryValue("createDate");
            if (!StringTool.isNull(queryValue)) {
                Date createDate = CalendarTool.parseDate(queryValue, "yyyy-MM-dd");
                Date begin = CalendarTool.beginningOfTheDay(createDate).getTime();
                Date end = CalendarTool.endingOfTheDay(createDate).getTime();
                detachedCriteria.add(Restrictions.between("createDate", begin, end));
            }
            queryValue = queryRequest.getQueryValue("modifyDate");
            if (!StringTool.isNull(queryValue)) {
                Date modifyDate = CalendarTool.parseDate(queryValue, "yyyy-MM-dd");
                Date begin = CalendarTool.beginningOfTheDay(modifyDate).getTime();
                Date end = CalendarTool.endingOfTheDay(modifyDate).getTime();
                detachedCriteria.add(Restrictions.between("modifyDate", begin, end));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("name"));
        detachedCriteria.addOrder(Order.asc("showOrder"));
        
        
        return this.findPageBy(detachedCriteria, pageIndex, pageSize);
    }
    
}
