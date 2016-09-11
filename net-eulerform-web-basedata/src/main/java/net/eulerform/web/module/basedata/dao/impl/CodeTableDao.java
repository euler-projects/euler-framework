package net.eulerform.web.module.basedata.dao.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import net.eulerform.common.email.EmailConfig;
import net.eulerform.common.util.StringTool;
import net.eulerform.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.basedata.dao.ICodeTableDao;
import net.eulerform.web.module.basedata.entity.CodeTable;

public class CodeTableDao extends BaseDao<CodeTable> implements ICodeTableDao {
    
    public final static int JS_DICT_TYPE = 1;
    public final static int PROPERTY_TYPE = 2;

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
    public PageResponse<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        detachedCriteria.addOrder(Order.asc("name"));
        detachedCriteria.addOrder(Order.asc("showOrder"));
        
        
        return this.findPageBy(detachedCriteria, pageIndex, pageSize);
    }

    @Override
    public EmailConfig findSysEmailConfig() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.add(Restrictions.eq("codeType", PROPERTY_TYPE));
        String[] emailConfigKey = new String[5];
        emailConfigKey[0] = EmailConfig.DB_CONFIG_KEY_UESRNAME;
        emailConfigKey[1] = EmailConfig.DB_CONFIG_KEY_PASSWORD;
        emailConfigKey[2] = EmailConfig.DB_CONFIG_KEY_SMTP;
        emailConfigKey[3] = EmailConfig.DB_CONFIG_KEY_SYS_SENDER;
        emailConfigKey[4] = EmailConfig.DB_CONFIG_KEY_DEFAULT_RECEIVER;
        detachedCriteria.add(Restrictions.in("key", emailConfigKey));
        List<CodeTable> result = this.findBy(detachedCriteria);
        if(result == null || result.isEmpty()) return null; 
        EmailConfig emailConfig = new EmailConfig();
        for(CodeTable each : result) {
            String key = each.getKey();
            switch (key) {
            case EmailConfig.DB_CONFIG_KEY_UESRNAME:emailConfig.setUsername(each.getValue());break;
            case EmailConfig.DB_CONFIG_KEY_PASSWORD:emailConfig.setPassword(each.getValue());break;
            case EmailConfig.DB_CONFIG_KEY_SMTP:emailConfig.setSmtp(each.getValue());break;
            case EmailConfig.DB_CONFIG_KEY_SYS_SENDER:emailConfig.setSender(each.getValue());break;
            case EmailConfig.DB_CONFIG_KEY_DEFAULT_RECEIVER:emailConfig.setDefaultReceiver(each.getValue());break;
            }
        }
        return  emailConfig;
    }
    
}
