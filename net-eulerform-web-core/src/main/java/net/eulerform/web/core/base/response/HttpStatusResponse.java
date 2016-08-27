package net.eulerform.web.core.base.response;

import org.springframework.http.HttpStatus;

/**
 * 用来作为Web的返回数据实体
 * 
 * @author cFrost
 */
public class HttpStatusResponse implements BaseResponse {

    /**
     * 新建空实体,并指定标准HTTP状态码{@link HttpStatus}
     * 
     * @param httpStatus
     *            标准HTTP状态码
     */
    public HttpStatusResponse(HttpStatus httpStatus) {
        this.statusCode = httpStatus.value();
        this.statusInfo = httpStatus.getReasonPhrase();
    }

    /**
     * 新建空实体,并指定非标准HTTP状态码和非标准HTTP状态信息
     * 
     * @param statusCode 状态码
     * @param statusInfo 状态信息
     */
    public HttpStatusResponse(int statusCode, String statusInfo) {
        this.statusCode = statusCode;
        this.statusInfo = statusInfo;
    }

    private Integer statusCode;

    private String statusInfo;

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getStatusInfo() {
        return statusInfo;
    }
}
