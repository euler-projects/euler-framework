package net.eulerform.web.module.cms.basic.entity;

import java.util.List;

public class ListResponse<T>  {
    
    private List<T> data;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
    
    public ListResponse() {}
    
    public ListResponse(List<T> data) {
        this.data = data;
    }
    
}
