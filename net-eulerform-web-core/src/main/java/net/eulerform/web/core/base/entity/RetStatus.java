package net.eulerform.web.core.base.entity;

public class RetStatus {
	
	public final static RetStatus SUCCESS = new RetStatus(0, "SUCCESS");
	public final static RetStatus RESROURCE_NOT_FOUND = new RetStatus(-1, "RESROURCE_NOT_FOUND");
	public final static RetStatus UNKNOWN_ERR = new RetStatus(-1000, "UNKNOWN_ERR");
	
	private Integer returnFlag;
	private String returnInfo;

	public RetStatus(Integer returnFlag, String returnInfo){
		this.returnFlag=returnFlag;
		this.returnInfo=returnInfo;
	}

	public Integer getReturnFlag() {
		return returnFlag;
	}

	public String getReturnInfo() {
		return returnInfo;
	}
}
