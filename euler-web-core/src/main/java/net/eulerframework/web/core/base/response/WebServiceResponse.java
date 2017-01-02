package net.eulerframework.web.core.base.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * 用来作为Webservice的返回数据实体
 * 
 * @author cFrost
 *
 * @param <T>
 *            返回数据类型,支持单个对象和容器<br>
 *            例如Object和List&lt;Object&gt;均只需指定为WebServiceResponse&lt;Object&gt;
 */
public class WebServiceResponse<T> extends HttpStatusResponse {

    private final List<T> data;

    /**
     * 新建单个对象WebService响应实体,状态码为200 OK
     * @param data 返回数据
     */
    public WebServiceResponse(T data) {
        super(HttpStatus.OK);        

        if(data == null)
            this.data = new ArrayList<>();
        else {
            List<T> dataList = new ArrayList<>();
            dataList.add(data);
            this.data = dataList;     
        }
    }
    
    /**
     * 新建多个对象WebService响应实体,状态码为200 OK
     * @param dataList 返回数据
     */
    public WebServiceResponse(List<T> dataList) {
        super(HttpStatus.OK);

        if(dataList == null)
            this.data = new ArrayList<>();
        else
            this.data = dataList;
    }
    
    /**
     * 指定标准HTTP状态码{@link HttpStatus}
     * 
     * @param httpStatus 标准HTTP状态码
     */
    public void changeStatus(HttpStatus httpStatus) {
        this.setStatus(httpStatus.value(), httpStatus.getReasonPhrase());
    }
    
    /**
     * 指定标准HTTP状态码{@link HttpStatus}和非标准的HTTP状态信息
     * 
     * @param httpStatus 标准HTTP状态码
     * @param statusInfo 非标准HTTP状态信息
     */
    public void changeStatus(HttpStatus httpStatus, String statusInfo) {
        this.setStatus(httpStatus.value(), statusInfo);
    }

    /**
     * 指定非标准HTTP状态码{@link Status}
     * 
     * @param status 非标准HTTP状态吗
     */
    public void changeStatus(Status status) {
        this.setStatus(status.value(), status.getReasonPhrase());
    }
    
    /**
     * 指定非标准HTTP状态码{@link Status}和非标准的HTTP状态信息
     * 
     * @param status 非标准HTTP状态吗
     * @param statusInfo 非标准HTTP状态信息
     */
    public void changeStatus(Status status, String statusInfo) {
        this.setStatus(status.value(), statusInfo);
    }

    public List<T> getData() {
        return data;
    }

    public Date getReturnDate() {
        return new Date();
    }
}
