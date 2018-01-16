package net.eulerframework.web.core.base.request;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.common.util.StringUtils;

/**
 * 用来接收POST传递进的form参数
 * @author cFrost
 *
 */
public class QueryRequest extends LogSupport implements BaseRequest {
    
    private final static String QUERY_PREFIX = "query.";
    private static final String MODE_PREFIX = "mode.";
    private static final String SORT_PARAM_NAME = "sort";
    private static final String ORDER_PARAM_NAME = "order";
    private static final String SPLIT = ",";
    
    private static final String CASE_SENSITIVE_NAME="caseSensitive";
    private static final String OR_QUERY_NAME="useOr";
    
    private boolean caseSensitive;
    private boolean useOr;
    
    private Map<String, QueryMode> modeMap = new HashMap<>();
    private LinkedHashMap<String, OrderMode> sortMap = new LinkedHashMap<>();
    
    private Map<String, String> queryMap = new HashMap<>();
    
    /**
     * 默认解析query.开头的参数
     * @param request
     */
    public QueryRequest(HttpServletRequest request){
        this.caseSensitive = Boolean.parseBoolean(request.getParameter(CASE_SENSITIVE_NAME));
        this.useOr = Boolean.parseBoolean(request.getParameter(OR_QUERY_NAME));
        
        this.queryMap = this.extractParams(request, QUERY_PREFIX);
        
        this.modeMap = this.extractParams(request, MODE_PREFIX, new ParamExtractor<QueryMode>() {

            @Override
            public QueryMode extract(String value) {
                switch(value) {
                case "is" : return QueryMode.IS;
                case "not" : return QueryMode.NOT;
                case "lt" : return QueryMode.LT;
                case "le" : return QueryMode.LE;
                case "gt" : return QueryMode.GT;
                case "ge" : return QueryMode.GE;
                case "in" : return QueryMode.IN;
                case "notin" : return QueryMode.NOTIN;
                case "between" : return QueryMode.BETWEEN;
                case "outside" : return QueryMode.OUTSIDE;
                case "exact" : return QueryMode.EXACT;
                case "anywhere" : return QueryMode.ANYWHERE;
                case "start" : return QueryMode.START;
                case "end" : return QueryMode.END;
                default:throw new IllegalArgumentException("unkonwn query mode "+ value);               
                }
            }
        });
        
        this.sortMap = this.extractOrderMode(request);
    }
    
    public String getQueryValue(String key){
        return this.queryMap.get(key);
    }

    public QueryMode getQueryMode(String property) {
        QueryMode ret = this.modeMap.get(property);
        
        if(ret == null)
            return QueryMode.IS;
        
        return ret;
    }
    
    public OrderMode getOrderMode(String property) {
        return this.sortMap.get(property);
    }
    
    public boolean caseSensitive() {
        return this.caseSensitive;
    }
    
    public boolean useOr() {
        return this.useOr;
    }
    
    private LinkedHashMap<String, OrderMode> extractOrderMode(HttpServletRequest request) {
        LinkedHashMap<String, OrderMode> result = new LinkedHashMap<>();
        
        String sorts = request.getParameter(SORT_PARAM_NAME);
        String orders = request.getParameter(ORDER_PARAM_NAME);
        
        if(StringUtils.isNull(sorts)) {
            return result;
        }
        
        if(StringUtils.isNull(orders)) {
            throw new IllegalArgumentException("order is required when request has sort params");
        }
        
        String[] sortArray = sorts.split(SPLIT);
        String[] orderArray = orders.split(SPLIT);
        
        if(sortArray.length > orderArray.length) {
            throw new IllegalArgumentException("Miss order params, require " + sortArray.length + "actually + " + orderArray.length);            
        } else if(sortArray.length < orderArray.length) {
            this.logger.warn("Request only has " + sortArray.length + " sort properties, but there are " + orderArray.length + " order params, ingnore the extra.");
        }
        
        for(int i = 0; i < sortArray.length; i++) {
            OrderMode ordermode;
            
            switch(orderArray[i]) {
            case"asc":ordermode = OrderMode.ASC;break;
            case"desc":ordermode = OrderMode.DESC;break;
            default:throw new IllegalArgumentException("unkonwn order mode "+ orderArray[i]);
            }
            
            result.put(sortArray[i], ordermode);
        }
        
        return result;
    }
    
    protected Map<String, String> extractParams(HttpServletRequest request, String prefix) {
        return this.extractParams(request, prefix, new ParamExtractor<String>() {

            @Override
            public String extract(String value) {
                return value;
            }
            
        });
    }
    
    protected <T> Map<String, T> extractParams(HttpServletRequest request, String prefix, ParamExtractor<T> paramFormatter) {
        Map<String, T> result = new HashMap<>();
        
        Map<String, String[]> map = request.getParameterMap();
        
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String key = entry.getKey();
            if(key.startsWith(prefix)) {
                String valueStr = entry.getValue().length > 0 ? entry.getValue()[0].trim() : null;
                if(StringUtils.isNull(valueStr))
                    continue;
                
                T value = paramFormatter.extract(valueStr);
                
                result.put(key.substring(prefix.length()), value);
            }
        }
        
        return result;
    }
    
    protected interface ParamExtractor <T> {
        public T extract(String value);
    }

    public enum QueryMode {
        IS, NOT, LT, LE, GT, GE, IN, NOTIN, BETWEEN, OUTSIDE, EXACT, ANYWHERE, START, END
    }
    
    public enum OrderMode {
        ASC, DESC
    }

    public Map<String, String> getQueryMap() {
        return this.queryMap;
    }

    public LinkedHashMap<String, OrderMode> getSortMap() {
        return this.sortMap;
    }

    public Map<String, QueryMode> getModeMap() {
        return modeMap;
    }

}
