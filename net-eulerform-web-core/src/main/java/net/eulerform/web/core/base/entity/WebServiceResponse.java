package net.eulerform.web.core.base.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WebServiceResponse<T> {
    
    public WebServiceResponse() {
        this.dataSize = 0;
    }

    public WebServiceResponse(T data) {
        this.setData(data);
    }

    public WebServiceResponse(List<T> data) {
        this.setData(data);
    }
    
    public WebServiceResponse(T data, WebServiceResponseStatus webServiceResponseStatus) {
        this();
        this.setData(data);
        this.statusCode = webServiceResponseStatus.getStatusCode();
        this.statusInfo = webServiceResponseStatus.getStatusInfo();
    }

    public WebServiceResponse(List<T> data, WebServiceResponseStatus webServiceResponseStatus) {
        this();
        this.setData(data);
        this.statusCode = webServiceResponseStatus.getStatusCode();
        this.statusInfo = webServiceResponseStatus.getStatusInfo();
    }
    
    public WebServiceResponse(WebServiceResponseStatus webServiceResponseStatus) {
		this();
		this.statusCode = webServiceResponseStatus.getStatusCode();
		this.statusInfo = webServiceResponseStatus.getStatusInfo();
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
    		this.setStatus(WebServiceResponseStatus.NO_CONTENT);
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

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatus(WebServiceResponseStatus webServiceResponseStatus) {
    	this.statusCode = webServiceResponseStatus.getStatusCode();
        this.statusInfo = webServiceResponseStatus.getStatusInfo();
    }
    
    public void setStatus(int statusCode, String statusInfo) {
        this.statusCode = statusCode;
        this.statusInfo = statusInfo;
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
