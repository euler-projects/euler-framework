package net.eulerform.web.core.cache;

import java.util.Date;

public class DataStore<T> {
    private final T data;
    private final long addTime;

    public DataStore(T data) {
        this.data = data;
        this.addTime = new Date().getTime();
    }

    public T getData() {
        return data;
    }

    public long getAddTime() {
        return addTime;
    }
}
