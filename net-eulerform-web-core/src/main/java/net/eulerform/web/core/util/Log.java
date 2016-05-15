package net.eulerform.web.core.util;

import net.eulerform.web.core.log.entity.AccessLog;
import net.eulerform.web.core.log.service.ILogService;

public class Log {

    private static ILogService logService;

    public void setLogService(ILogService logService) {
        Log.logService = logService;
    }
    
    public static void saveAccessLog(AccessLog accessLog){
        logService.saveAccessLog(accessLog);
    }
}
