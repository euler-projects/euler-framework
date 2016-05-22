package net.eulerform.web.core.base.entity;

public class RestResponseStatus {
	
	public final static RestResponseStatus SUCCESS = new RestResponseStatus(0, "SUCCESS");
	public final static RestResponseStatus SERVICE_NOT_FOUND = new RestResponseStatus(404, "SERVICE_NOT_FOUND");
	public final static RestResponseStatus RESROURCE_NOT_FOUND = new RestResponseStatus(-1, "RESROURCE_NOT_FOUND");
	public final static RestResponseStatus UNKNOWN_ERR = new RestResponseStatus(-1000, "UNKNOWN_ERR");
	
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
