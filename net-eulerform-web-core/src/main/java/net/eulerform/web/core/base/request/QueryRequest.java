package net.eulerform.web.core.base.request;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 用来接收POST传递进的form参数
 * @author cFrost
 *
 */
public class QueryRequest implements BaseRequest {
    
    private final static String QUERY_PREFIX = "query.";
    
    public Map<String, String> values = new HashMap<>();
    
    /**
     * 默认解析query.开头的参数
     * @param request
     */
    public QueryRequest(HttpServletRequest request){        
        this(request, QUERY_PREFIX);
    }
    
    /**
     * 可指定参数的前缀
     * @param request
     */
    public QueryRequest(HttpServletRequest request, String prefix){
        
        Map<String, String[]> map = request.getParameterMap();
        
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String key = entry.getKey();
            if(key.startsWith(prefix)) {
                String value = entry.getValue().length > 0 ? entry.getValue()[0].trim() : null;
                values.put(key.substring(prefix.length()), value);
            }
        }
    }
    
    public String getQueryValue(String key){
        return this.values.get(key);
    }

}
