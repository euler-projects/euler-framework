package net.eulerform.web.core.base.cache;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectCache<T> {
    private final ConcurrentHashMap<String, DataStore<T>> dataMap = new ConcurrentHashMap<>();
    
    private long dataLife;
    
    public ObjectCache(){
        this.dataLife = 0L;
    }
    
    public ObjectCache(long dataLife){
        this.dataLife = dataLife;
    }
    
    public void setDataLife(long milliseconds) {
        this.dataLife = milliseconds;
    }
    
    public void put(String key, T data){
        this.dataMap.put(key, new DataStore<T>(data));
    }
    
    public void remove(String key){
        this.dataMap.remove(key);
    }
    
    public T get(String key) {
        DataStore<T> cacheStore = this.dataMap.get(key);
        
        if(cacheStore == null)
            return null;
        
        if((new Date().getTime() - cacheStore.getAddTime()) >= this.dataLife) {
            this.dataMap.remove(key);
            return null;
        }
        
        return cacheStore.getData();
    }

}
