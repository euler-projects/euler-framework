package net.eulerform.web.core.base.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RetResult<T> {
    
    public RetResult() {
        this.dataSize = 0;
    }

    public RetResult(T data) {
        this.setData(data);
    }

    public RetResult(List<T> data) {
        this.setData(data);
    }
    
    public RetResult(RetStatus retStatus) {
		this();
		this.returnFlag = retStatus.getReturnFlag();
		this.returnInfo = retStatus.getReturnInfo();
	}

	private List<T> data;
    
    private Integer dataSize;
    
    private Integer returnFlag;
    
    private String returnInfo;
    
    private Date returnDate;
    
    public void setData(T data) {
        List<T> dataList = new ArrayList<>();
        dataList.add(data);
        this.setData(dataList);
    }
    
    public void setData(List<T> data) {
        this.data = data;
        this.dataSize = this.data==null?0:this.data.size();
    }
    
    public List<T> getData() {
        return data;
    }
    
    public Integer getReturnFlag() {
        return returnFlag;
    }
    
    public void setReturnFlag(Integer returnFlag) {
        this.returnFlag = returnFlag;
    }

    public void setReturnFlag(RetStatus returnFlag) {
        this.returnFlag = returnFlag.getReturnFlag();
    }

    public String getReturnInfo() {
        return returnInfo;
    }

    public void setReturnInfo(String returnInfo) {
        this.returnInfo = returnInfo;
    }

    public void setReturnInfo(RetStatus returnInfo) {
        this.returnInfo = returnInfo.getReturnInfo();
    }

    public void setReturnStatus(RetStatus retStatus) {
    	this.returnFlag = retStatus.getReturnFlag();
        this.returnInfo = retStatus.getReturnInfo();
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
