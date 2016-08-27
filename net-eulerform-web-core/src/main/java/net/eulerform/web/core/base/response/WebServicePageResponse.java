package net.eulerform.web.core.base.response;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 支持分页的Webservice返回数据实体
 * 
 * @author cFrost
 *
 * @param <T>
 *            返回数据类型
 */
@XmlRootElement
public class WebServicePageResponse<T> extends WebServiceResponse<T> {
    
    public WebServicePageResponse (PageResponse<T> pageResponse) {
        super(pageResponse.getRows());
        this.total = pageResponse.getTotal();
        this.pageIndex = pageResponse.getPageIndex();
        this.pageSize = pageResponse.getPageSize();
    }
    private Long total;    
    private Integer pageIndex;    
    private Integer pageSize;
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
