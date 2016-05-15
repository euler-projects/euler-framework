package net.eulerform.web.core.log.service.impl;

import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.log.dao.IAccessLogDao;
import net.eulerform.web.core.log.entity.AccessLog;
import net.eulerform.web.core.log.service.ILogService;

public class LogService extends BaseService implements ILogService {

    private IAccessLogDao accessLogDao;
    
    public void setAccessLogDao(IAccessLogDao accessLogDao) {
        this.accessLogDao = accessLogDao;
    }

    @Override
    public void saveAccessLog(AccessLog accessLog) {
        this.accessLogDao.saveOrUpdate(accessLog);
    }

}
