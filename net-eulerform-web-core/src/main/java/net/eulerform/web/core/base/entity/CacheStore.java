package net.eulerform.web.core.base.entity;

import java.util.Date;

public class CacheStore<T> {
    private T data;
    private Date addDate;

    public CacheStore(T data) {
        this.data = data;
        this.addDate = new Date();
    }

    public T getData() {
        return data;
    }

    public Date getAddDate() {
        return addDate;
    }
}
