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
        super(request);
        
        String pageSizeStr = request.getParameter(PAGE_SIZE_NAME);        
        if(StringTool.isNull(pageSizeStr)){
            throw new IllegalParamException("Param 'pageSize' is required");
        }        
        this.pageSize = Integer.parseInt(request.getParameter(PAGE_SIZE_NAME));
        
        if(this.pageSize <= 0) {
            this.pageIndex = -1;
        } else {
            String pageIndexStr = request.getParameter(PAGE_INDEX_NAME);        
            if(StringTool.isNull(pageIndexStr)){
                throw new IllegalParamException("Param 'pageIndex' is required");
            }        
            this.pageIndex = Integer.parseInt(request.getParameter(PAGE_INDEX_NAME));
            
            if(this.pageIndex < 0) {
                throw new IllegalParamException("Param 'pageIndex' must larger than 0");                
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
