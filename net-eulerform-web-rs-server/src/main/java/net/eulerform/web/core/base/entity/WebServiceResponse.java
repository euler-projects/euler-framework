package net.eulerform.web.core.base.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
@XmlRootElement
public class WebServiceResponse<T> extends HttpStatusResponse {

    /**
     * 新建空实体,状态代码默认为204 No Content
     */
    public WebServiceResponse() {
        super();
        this.setStatusToNoContent();
    }

    /**
     * 新建空实体,并指定标准HTTP状态码{@link HttpStatus}
     * 
     * @param httpStatus
     *            标准HTTP状态码
     */
    public WebServiceResponse(HttpStatus httpStatus) {
        this.data = null;
        this.dataSize = 0;
        this.setStatus(httpStatus);
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
        if (data == null) {
            this.setStatusToNoContent();
            return;
        }
        this.setData(data);
        this.setStatus(HttpStatus.OK);
    }

    /**
     * 新建容器实体,如果数据不为空则状态码为200 OK
     * 
     * @param data
     *            返回数据容器
     */
    public WebServiceResponse(List<T> data) {
        if (data == null || data.isEmpty()) {
            this.setStatusToNoContent();
            return;
        }
        this.setData(data);
        this.setStatus(HttpStatus.OK);
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
        this.setData(data);
        this.setStatus(httpStatus);
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
        this.setData(data);
        this.setStatus(httpStatus);
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
        this(statusCode, statusInfo);
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

    public void setData(T data) {
        if (data == null) {
            this.setStatusToNoContent();
            return;
        }

        List<T> dataList = new ArrayList<>();
        dataList.add(data);
        this.setData(dataList);
    }

    public void setData(List<T> data) {
        if (data == null || data.isEmpty()) {
            this.setStatusToNoContent();
            return;
        }

        this.data = data;
        this.dataSize = data.size();
    }

    @XmlElement
    public List<T> getData() {
        return data;
    }

    @XmlElement
    public Integer getDataSize() {
        return this.dataSize;
    }

    @XmlElement
    public Date getReturnDate() {
        return returnDate == null ? new Date() : this.returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    private void setStatusToNoContent() {
        this.data = null;
        this.dataSize = 0;
        this.setStatus(HttpStatus.NO_CONTENT);
    }
}
