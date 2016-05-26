package net.eulerform.web.core.base.entity;

import org.springframework.http.HttpStatus;

public class WebServiceResponseStatus {
	
	public final static WebServiceResponseStatus OK = new WebServiceResponseStatus(HttpStatus.OK);
	public final static WebServiceResponseStatus RESOURCE_NOT_FOUND = new WebServiceResponseStatus(HttpStatus.NOT_FOUND);
	public final static WebServiceResponseStatus NO_CONTENT = new WebServiceResponseStatus(HttpStatus.NO_CONTENT);
	public final static WebServiceResponseStatus UNKNOWN_ERROR = new WebServiceResponseStatus(-1, "Unknown Error");
	
	private Integer statusCode;
	private String statusInfo;

	private WebServiceResponseStatus(Integer statusCode, String statusInfo){
		this.statusCode=statusCode;
		this.statusInfo=statusInfo;
	}
	
	private WebServiceResponseStatus(HttpStatus httpStatus){
        this.statusCode=httpStatus.value();
        this.statusInfo=httpStatus.getReasonPhrase();
    }

	public Integer getStatusCode() {
		return statusCode;
	}

	public String getStatusInfo() {
		return statusInfo;
	}

}
