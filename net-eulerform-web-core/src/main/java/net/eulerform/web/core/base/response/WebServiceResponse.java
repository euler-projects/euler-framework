package net.eulerform.web.core.base.response;

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

    /**
     * 新建空实体,并指定标准HTTP状态码{@link HttpStatus}
     * 
     * @param httpStatus
     *            标准HTTP状态码
     */
    public WebServiceResponse(HttpStatus httpStatus) {
        super(httpStatus);
        this.data = null;
        this.dataSize = 0;
    }

    /**
     * 新建空实体,并指定非标准HTTP状态码和非标准HTTP状态信息
     * 
     * @param statusCode 状态码
     * @param statusInfo 状态信息
     */
    public WebServiceResponse(int statusCode, String statusInfo) {
        super(statusCode, statusInfo);
    }

    /**
     * 新建单个对象实体,如果数据不为空则状态码为200 OK
     * 
     * @param data
     *            返回数据
     */
    public WebServiceResponse(T data) {
        super(HttpStatus.OK);
        if (data == null) {
            return;
        }
        this.setData(data);
    }

    /**
     * 新建容器实体,如果数据不为空则状态码为200 OK
     * 
     * @param data
     *            返回数据容器
     */
    public WebServiceResponse(List<T> data) {
        super(HttpStatus.OK);
        if (data == null || data.isEmpty()) {
            return;
        }
        this.setData(data);
    }

    /**
     * 新建单个对象实体,并指定标准HTTP状态码{@link HttpStatus}
     * 
     * @param data
     *            返回数据
     * @param httpStatus
     *            标准HTTP状态码
     */
    public WebServiceResponse(T data, HttpStatus httpStatus) {
        super(httpStatus);
        this.setData(data);
    }

    /**
     * 新建容器实体,并指定标准HTTP状态码{@link HttpStatus}
     * 
     * @param data
     *            返回数据容器
     * @param httpStatus
     *            标准HTTP状态码
     */
    public WebServiceResponse(List<T> data, HttpStatus httpStatus) {
        super(httpStatus);
        this.setData(data);
    }

    /**
     * 新建单个对象实体,并指定非标准HTTP状态码和非标准HTTP状态信息
     * 
     * @param data
     *            返回数据
     * @param statusCode 状态码
     * @param statusInfo 状态信息
     */
    public WebServiceResponse(T data, int statusCode, String statusInfo) {
        super(statusCode, statusInfo);
        this.setData(data);
    }

    /**
     * 新建容器实体,并指定非标准HTTP状态码和非标准HTTP状态信息
     * 
     * @param data
     *            返回数据容器
     * @param statusCode 状态码
     * @param statusInfo 状态信息
     */
    public WebServiceResponse(List<T> data, int statusCode, String statusInfo) {
        this(statusCode, statusInfo);
        this.setData(data);
    }

    private List<T> data;

    private Integer dataSize;

    private Date returnDate;

    private void setData(T data) {
        if (data == null) {
            return;
        }

        List<T> dataList = new ArrayList<>();
        dataList.add(data);
        this.setData(dataList);
    }

    private void setData(List<T> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        this.data = data;
        this.dataSize = data.size();
    }

    public List<T> getData() {
        return data;
    }

    public Integer getDataSize() {
        return this.dataSize;
    }

    public Date getReturnDate() {
        return returnDate == null ? new Date() : this.returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }
}
