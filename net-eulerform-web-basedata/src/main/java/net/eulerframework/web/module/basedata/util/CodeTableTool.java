package net.eulerframework.web.module.basedata.util;

import net.eulerframework.common.email.EmailConfig;
import net.eulerframework.web.module.basedata.service.IBaseDataService;

public class CodeTableTool {

    private static IBaseDataService baseDataService;    
    
    public void setBaseDataService(IBaseDataService baseDataService) {
        CodeTableTool.baseDataService = baseDataService;
    }

    public static String findConfig(String key) {
        return baseDataService.findConfigValue(key);
    }
    
    public static EmailConfig findEmailConfig() {
        return baseDataService.findEmailConfig();
    }
}
