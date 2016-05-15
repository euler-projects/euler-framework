package net.eulerform.web.core.log.service;

import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.core.log.entity.AccessLog;

public interface ILogService extends IBaseService {

    public void saveAccessLog(AccessLog accessLog);
}
