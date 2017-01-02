package net.eulerframework.web.core.base.response;

import org.springframework.http.HttpStatus;

/**
 * 只返回状态码和状态信息
 * 
 * @author cFrost
 */
public class HttpStatusResponse implements BaseResponse {

    /**
     * 新建空实体,并指定标准HTTP状态码{@link HttpStatus}
     * 
     * @param httpStatus 标准HTTP状态码
     */
    public HttpStatusResponse(HttpStatus httpStatus) {
        this.statusCode = httpStatus.value();
        this.statusInfo = httpStatus.getReasonPhrase();
    }
    
    /**
     * 新建空实体,并指定标准HTTP状态码{@link HttpStatus}和非标准的HTTP状态信息
     * 
     * @param httpStatus 标准HTTP状态码
     * @param statusInfo 非标准HTTP状态信息
     */
    public HttpStatusResponse(HttpStatus httpStatus, String statusInfo) {
        this.statusCode = httpStatus.value();
        this.statusInfo = statusInfo;
    }

    /**
     * 新建空实体,并指定非标准HTTP状态码{@link Status}
     * 
     * @param status 非标准HTTP状态吗
     */
    public HttpStatusResponse(Status status) {
        this.statusCode = status.value();
        this.statusInfo = status.getReasonPhrase();
    }
    
    /**
     * 新建空实体,并指定非标准HTTP状态码{@link Status}和非标准的HTTP状态信息
     * 
     * @param status 非标准HTTP状态吗
     * @param statusInfo 非标准HTTP状态信息
     */
    public HttpStatusResponse(Status status, String statusInfo) {
        this.statusCode = status.value();
        this.statusInfo = status.getReasonPhrase();
    }
    
    protected HttpStatusResponse() {}

    private int statusCode;

    private String statusInfo;

    protected void setStatus(int statusCode, String statusInfo) {
        this.statusCode = statusCode;
        this.statusInfo = statusInfo;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusInfo() {
        return statusInfo;
    }
}
