package net.eulerframework.web.core.base.request.easyuisupport;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.eulerframework.web.core.base.request.PageQueryRequest;

public class EasyUiQueryReqeuset extends PageQueryRequest {
    
    private final static String FILTER_PREFIX = "filter.";
    
    private Map<String, String> filterMap = new HashMap<>();

    public EasyUiQueryReqeuset(HttpServletRequest request) {
        super(request, "page", "rows");
        
        this.filterMap = this.extractParams(request, FILTER_PREFIX);
    }
    
    public String getFilterValue(String key){
        return this.filterMap.get(key);
    }

}
