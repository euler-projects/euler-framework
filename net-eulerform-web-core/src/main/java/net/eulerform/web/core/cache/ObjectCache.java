package net.eulerform.web.core.cache;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectCache<KEY_T, DATA_T> {
    private final ConcurrentHashMap<KEY_T, DataStore<DATA_T>> dataMap = new ConcurrentHashMap<>();
    
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
    
    public void put(KEY_T key, DATA_T data){
        this.dataMap.put(key, new DataStore<DATA_T>(data));
    }
    
    public void remove(KEY_T key){
        this.dataMap.remove(key);
    }
    
    public DATA_T get(KEY_T key) {
        DataStore<DATA_T> cacheStore = this.dataMap.get(key);
        
        if(cacheStore == null)
            return null;
        
        if((new Date().getTime() - cacheStore.getAddTime()) >= this.dataLife) {
            this.dataMap.remove(key);
            return null;
        }
        
        return cacheStore.getData();
    }

}
