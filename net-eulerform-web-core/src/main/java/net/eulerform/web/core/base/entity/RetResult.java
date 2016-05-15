package net.eulerform.web.core.base.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RetResult<T> {
    
    public final static int UNINIT = 0;
    public final static int SUCCESS = 1;
    public final static int UNKNOWN_ERR = -1;
    
    public RetResult() {
        this.dataSize = 0;
        this.returnFlag = RetResult.UNINIT;
        this.returnInfo = "UNINIT";
    }

    public RetResult(T data) {
        this.setData(data);
    }

    public RetResult(List<T> data) {
        this.setData(data);
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
        if(this.returnInfo == "UNINIT") {
            this.returnInfo = null;
        }
    }
    
    public List<T> getData() {
        return data;
    }
    
    public Integer getReturnFlag() {
        return returnFlag;
    }
    
    public void setReturnFlag(Integer returnFlag) {
        this.returnFlag = returnFlag;
        
        if(this.returnInfo == null || this.returnInfo == "UNINIT") {
            switch(returnFlag) {
            case RetResult.SUCCESS : this.returnInfo = "SUCCESS";break;
            case RetResult.UNKNOWN_ERR : this.returnInfo = "UNKNOWN_ERR";break;
            }
        }
    }

    public String getReturnInfo() {
        return returnInfo;
    }

    public void setReturnInfo(String returnInfo) {
        this.returnInfo = returnInfo;
    }

    public void setDataSize(Integer dataSize) {
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
