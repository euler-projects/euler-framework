package net.eulerform.web.core.base.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

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
public class WebServiceResponse<T> {

    /**
     * 新建空实体,状态代码默认为204 No Content
     */
    public WebServiceResponse() {
        this.setStatusToNoContent();
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
     * 新建单个对象实体,并指定非标准HTTP状态码{@link WebServiceResponseStatus}
     * 
     * @param data
     *            返回数据
     * @param webServiceResponseStatus
     *            非标准HTTP状态码
     */
    public WebServiceResponse(T data, WebServiceResponseStatus webServiceResponseStatus) {
        this.setData(data);
        this.setStatus(webServiceResponseStatus);
    }

    /**
     * 新建容器实体,并指定非标准HTTP状态码{@link WebServiceResponseStatus}
     * 
     * @param data
     *            返回数据容器
     * @param webServiceResponseStatus
     *            非标准HTTP状态码
     */
    public WebServiceResponse(List<T> data, WebServiceResponseStatus webServiceResponseStatus) {
        this.setData(data);
        this.setStatus(webServiceResponseStatus);
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
     * 新建空实体,并指定非标准HTTP状态码{@link WebServiceResponseStatus}
     * 
     * @param webServiceResponseStatus
     *            非标准HTTP状态码
     */
    public WebServiceResponse(WebServiceResponseStatus webServiceResponseStatus) {
        this.data = null;
        this.dataSize = 0;
        this.setStatus(webServiceResponseStatus);
    }

    private List<T> data;

    private Integer dataSize;

    private Integer statusCode;

    private String statusInfo;

    private Date returnDate;

    @JsonIgnore
    public void setData(T data) {
        if (data == null) {
            this.setStatusToNoContent();
            return;
        }

        List<T> dataList = new ArrayList<>();
        dataList.add(data);
        this.setData(dataList);
    }

    @JsonSetter
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
    @JsonSetter
    public Integer getStatusCode() {
        return statusCode;
    }

    @XmlElement
    @JsonSetter
    public String getStatusInfo() {
        return statusInfo;
    }

    @JsonIgnore
    public void setStatus(HttpStatus httpStatus) {
        this.statusCode = httpStatus.value();
        this.statusInfo = httpStatus.getReasonPhrase();
    }

    @JsonIgnore
    public void setStatus(WebServiceResponseStatus webServiceResponseStatus) {
        this.statusCode = webServiceResponseStatus.getStatusCode();
        this.statusInfo = webServiceResponseStatus.getStatusInfo();
    }

    @JsonIgnore
    public void setStatus(int statusCode, String statusInfo) {
        this.statusCode = statusCode;
        this.statusInfo = statusInfo;
    }

    @XmlElement
    public Integer getDataSize() {
        return this.dataSize;
    }

    @XmlElement
    public Date getReturnDate() {
        return returnDate == null ? new Date() : this.returnDate;
    }

    @JsonSetter
    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    private void setStatusToNoContent() {
        this.data = null;
        this.dataSize = 0;
        this.setStatus(HttpStatus.NO_CONTENT);
    }
}
