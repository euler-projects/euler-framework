package net.eulerform.web.core.base.entity;

public class RestResponseStatus {
	
	public final static RestResponseStatus OK = new RestResponseStatus(200, "OK");
	public final static RestResponseStatus SERVICE_NOT_FOUND = new RestResponseStatus(404, "SERVICE_NOT_FOUND");
	public final static RestResponseStatus NO_CONTENT = new RestResponseStatus(204, "NO_CONTENT");
	public final static RestResponseStatus UNKNOWN_ERR = new RestResponseStatus(-1, "UNKNOWN_ERR");
	
	private Integer statusCode;
	private String statusInfo;

	public RestResponseStatus(Integer statusCode, String statusInfo){
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
