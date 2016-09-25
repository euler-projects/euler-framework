package net.eulerform.web.module.basedata.util;

import net.eulerform.common.email.EmailConfig;
import net.eulerform.web.module.basedata.service.IBaseDataService;

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
