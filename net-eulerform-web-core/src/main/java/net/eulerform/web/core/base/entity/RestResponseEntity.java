package net.eulerform.web.core.base.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RestResponseEntity<T> {
    
    public RestResponseEntity() {
        this.dataSize = 0;
    }

    public RestResponseEntity(T data) {
        this.setData(data);
    }

    public RestResponseEntity(List<T> data) {
        this.setData(data);
    }
    
    public RestResponseEntity(T data, RestResponseStatus restResponseStatus) {
        this();
        this.setData(data);
        this.statusCode = restResponseStatus.getStatusCode();
        this.statusInfo = restResponseStatus.getStatusInfo();
    }

    public RestResponseEntity(List<T> data, RestResponseStatus restResponseStatus) {
        this();
        this.setData(data);
        this.statusCode = restResponseStatus.getStatusCode();
        this.statusInfo = restResponseStatus.getStatusInfo();
    }
    
    public RestResponseEntity(RestResponseStatus restResponseStatus) {
		this();
		this.statusCode = restResponseStatus.getStatusCode();
		this.statusInfo = restResponseStatus.getStatusInfo();
	}

	private List<T> data;
    
    private Integer dataSize;
    
    private Integer statusCode;
    
    private String statusInfo;
    
    private Date returnDate;
    
    public void setData(T data) {
        List<T> dataList = new ArrayList<>();
        
        if(data != null)
        	dataList.add(data);
        
        this.setData(dataList);
    }
    
    public void setData(List<T> data) {
    	if(data == null || data.isEmpty()){
    		this.data=null;
    		this.dataSize=0;
    		this.setStatus(RestResponseStatus.NO_CONTENT);
    		return;
    	}
    	
        this.data = data;
        this.dataSize = data.size();
    }
    
    public List<T> getData() {
        return data;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusCode(RestResponseStatus statusCode) {
        this.statusCode = statusCode.getStatusCode();
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public void setStatusInfo(RestResponseStatus statusInfo) {
        this.statusInfo = statusInfo.getStatusInfo();
    }

    public void setStatus(RestResponseStatus restResponseStatus) {
    	this.statusCode = restResponseStatus.getStatusCode();
        this.statusInfo = restResponseStatus.getStatusInfo();
    }

    public Integer getDataSize() {
        return this.dataSize;
    }

    public Date getReturnDate() {
        return returnDate == null?new Date():this.returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }
}
