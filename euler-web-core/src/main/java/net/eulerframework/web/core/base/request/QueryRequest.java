package net.eulerframework.web.core.base.request;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.base.exception.BadRequestException;

/**
 * 用来接收POST传递进的form参数
 * @author cFrost
 *
 */
public class QueryRequest implements BaseRequest {
    
    private final static String QUERY_PREFIX = "query.";
    private final static String MATCH_PREFIX = "match.";
    private final static String SORT_PARAM_NAME = "sort";
    private final static String ORDER_PARAM_NAME = "order";
    private final static String SPLIT_PARAM_NAME = "split";
    private final static String SPLIT = ",";
    
    public Map<String, String> queryValueMap = new HashMap<>();
    public Map<String, EulerMatchMode> matchModeMap = new HashMap<>();
    public Map<String, EulerOrderMode> orderModeMap = new HashMap<>();
    
    /**
     * 默认解析query.开头的参数
     * @param request
     */
    public QueryRequest(HttpServletRequest request){        
        this(request, 
             QUERY_PREFIX,
             MATCH_PREFIX,
             SORT_PARAM_NAME,
             ORDER_PARAM_NAME,
             SPLIT_PARAM_NAME);
    }
    
    public QueryRequest(
            HttpServletRequest request, 
            String queryPrefix, 
            String machModePrefix, 
            String sortParamName, 
            String orderParamName,
            String splitParamName){
        this.queryValueMap = this.generateRequestMap(request, queryPrefix);
        
        //this.matchModeMap = this.generateRequestMap(request, machModePrefix);
        this.orderModeMap = this.generateOrderMap(request, sortParamName, orderParamName, splitParamName);
        
    }
    
    private Map<String, String> generateRequestMap(HttpServletRequest request, String prefix) {
        Map<String, String> result = new HashMap<>();
        Map<String, String[]> map = request.getParameterMap();
        
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String key = entry.getKey();
            if(key.startsWith(prefix)) {
                String value = entry.getValue().length > 0 ? entry.getValue()[0].trim() : null;
                result.put(key.substring(prefix.length()), value);
            }
        }
        return result;
    }
    
    private Map<String, EulerOrderMode> generateOrderMap(
            HttpServletRequest request, 
            String sortParamName,
            String orderParamName,
            String splitParamName) {
        Map<String, EulerOrderMode> result = new HashMap<>();

        String sortStr = request.getParameter(sortParamName);
        
        if(StringTool.isNull(sortStr))
            return result;
        
        String orderStr = request.getParameter(orderParamName);
        
        if(StringTool.isNull(orderStr))
            throw new BadRequestException("miss order mode");
        
        String split = request.getParameter(splitParamName);

        if(StringTool.isNull(split))
            split = SPLIT;
        
        String[] sortArray = sortStr.split(split);
        String[] orderArray = orderStr.split(split);
        
        if(sortArray.length > orderArray.length) {
            throw new BadRequestException("miss order mode: sort property is " + sortArray.length 
                    + ", but order is "+ orderArray.length);
        }
        
        for(int i = 0 ; i < sortArray.length; i++) {
            String sort = sortArray[i];
            String order = orderArray[i];
            try {
                result.put(sort, this.getEulerOrderMode(order));
            } catch (QueryRequestException e) {
                throw new BadRequestException("unknown order mode: " + order);
            }
        }
        
        return result;
    }
    
    public String getQueryValue(String key){
        return this.queryValueMap.get(key);
    }
    
    public EulerMatchMode getMatchMode(String key){
        return this.matchModeMap.get(key);
    }
    
    public EulerOrderMode getOrderMode(String key){
        return this.orderModeMap.get(key);
    }

    public enum EulerMatchMode {
        EXACT,START,END,ANYWHERE;
    }
    
    public enum EulerOrderMode {
        ASC,DESC;
    }
    
    private EulerMatchMode getEulerMatchMode(String matchMode) throws QueryRequestException {            
        switch(matchMode) {
            case "exact" : return EulerMatchMode.EXACT;
            case "start" : return EulerMatchMode.START;
            case "end" : return EulerMatchMode.END;
            case "anywhere" : return EulerMatchMode.ANYWHERE;
            default : throw new QueryRequestException("unkonwn match mode: " + matchMode);
        }            
    }
    
    private EulerOrderMode getEulerOrderMode(String orderMode) throws QueryRequestException {
        switch(orderMode) {
            case "asc" : return EulerOrderMode.ASC;
            case "desc" : return EulerOrderMode.DESC;
            default : throw new QueryRequestException("unkonwn order mode: " + orderMode);
        }           
    }
    
    public class QueryRequestException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public QueryRequestException() {
            super();
        }

        public QueryRequestException(String message) {
            super(message);
        }

        public QueryRequestException(String message, Throwable cause) {
            super(message, cause);
        }

        public QueryRequestException(Throwable cause) {
            super(cause);
        }
        
        protected QueryRequestException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

    }
}
