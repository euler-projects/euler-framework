package net.eulerform.web.module.basedata.util;

import net.eulerform.web.module.basedata.service.IBaseDataService;

public class CodeTable {

    private static IBaseDataService baseDataService;    
    
    public void setBaseDataService(IBaseDataService baseDataService) {
        CodeTable.baseDataService = baseDataService;
    }

    public static String findConfig(String key) {
        return baseDataService.findConfigValue(key);
    }
}
