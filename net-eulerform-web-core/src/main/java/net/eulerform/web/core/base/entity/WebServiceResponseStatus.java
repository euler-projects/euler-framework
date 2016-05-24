package net.eulerform.web.core.base.entity;

public class WebServiceResponseStatus {
	
	public final static WebServiceResponseStatus OK = new WebServiceResponseStatus(200, "OK");
	public final static WebServiceResponseStatus SERVICE_NOT_FOUND = new WebServiceResponseStatus(404, "SERVICE_NOT_FOUND");
	public final static WebServiceResponseStatus NO_CONTENT = new WebServiceResponseStatus(204, "NO_CONTENT");
	public final static WebServiceResponseStatus UNKNOWN_ERR = new WebServiceResponseStatus(-1, "UNKNOWN_ERR");
	
	private Integer statusCode;
	private String statusInfo;

	private WebServiceResponseStatus(Integer statusCode, String statusInfo){
		this.statusCode=statusCode;
		this.statusInfo=statusInfo;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public String getStatusInfo() {
		return statusInfo;
	}

}
