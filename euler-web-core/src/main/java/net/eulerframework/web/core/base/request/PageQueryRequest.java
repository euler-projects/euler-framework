package net.eulerframework.web.core.base.request;

import javax.servlet.http.HttpServletRequest;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.exception.IllegalParamException;

public class PageQueryRequest extends QueryRequest {

    private static final String PAGE_SIZE_NAME = "pageSize";
    private static final String PAGE_INDEX_NAME = "pageIndex";
    
    private int pageIndex;
    private int pageSize;

    public PageQueryRequest(HttpServletRequest request) {
        this(request, PAGE_INDEX_NAME, PAGE_SIZE_NAME);
    }
    public PageQueryRequest(HttpServletRequest request, String pageIndexParamName, String pageSizeParamName) {
        super(request);
        
        String pageSizeStr = request.getParameter(pageSizeParamName);        
        if(StringTool.isNull(pageSizeStr)){
            throw new IllegalParamException("Param '" +pageSizeParamName+ "' is required");
        }        
        this.pageSize = Integer.parseInt(request.getParameter(pageSizeParamName));
        
        if(this.pageSize <= 0) {
            this.pageIndex = -1;
        } else {
            String pageIndexStr = request.getParameter(pageIndexParamName);        
            if(StringTool.isNull(pageIndexStr)){
                throw new IllegalParamException("Param '" +pageIndexParamName+ "' is required");
            }        
            this.pageIndex = Integer.parseInt(request.getParameter(pageIndexParamName));
            
            if(this.pageIndex < 0) {
                throw new IllegalParamException("Param '" +pageIndexParamName+ "' must larger than 0");                
            }
        }
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }
    
    public boolean enablePageQuery() {
        return this.pageSize > 0;
    }
}
