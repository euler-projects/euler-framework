package net.eulerform.web.core.base.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来作为分页查询的返回数据实体
 * 
 * @author cFrost
 *
 * @param <T>
 *            返回数据类型,支持单个对象和容器<br>
 *            例如Object和List&lt;Object&gt;均只需指定为PageResponse&lt;Object&gt;
 */
public class PageResponse<T> {

    private List<T> rows;

    private Long total;
    
    private Integer pageIndex;
    
    private Integer pageSize;

    public void setRows(T data) {
        if (data == null) {
            this.rows = new ArrayList<>();
            return;
        }

        List<T> dataList = new ArrayList<>();
        dataList.add(data);
        this.setRows(dataList);
    }

    public void setRows(List<T> data) {
        if (data == null || data.isEmpty()) {
            this.rows = new ArrayList<>();
            return;
        }

        this.rows = data;
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

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
