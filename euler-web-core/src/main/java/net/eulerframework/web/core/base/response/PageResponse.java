package net.eulerframework.web.core.base.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来作为分页查询的响应实体
 * 
 * @author cFrost
 *
 * @param <T> 返回数据类型
 */
public class PageResponse<T> implements BaseResponse {

    private final List<T> rows;

    private final long total;
    
    private final int pageIndex;
    
    private final int pageSize;
    
    /**
     * 新建分页响应实体
     * @param rows 分页数据
     * @param total 数据总数
     * @param pageIndex 页码
     * @param pageSize 每页数据量
     */
    public PageResponse(List<T> rows, long total, int pageIndex, int pageSize) {
        if(rows == null)
            this.rows = new ArrayList<>();
        else
            this.rows = rows;
        
        this.total = total;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }
    
    public List<T> getRows() {
        return rows;
    }
    
    public Long getTotal() {
        return total;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }
}
