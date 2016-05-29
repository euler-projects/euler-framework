package net.eulerform.web.core.base.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.eulerform.web.core.base.entity.BaseEntity;

public class EntityCache<T extends BaseEntity<?>> {
    private Map<String, CacheStore<T>> dataMap = new HashMap<>();
    
    private long dataLife;
    
    public EntityCache(){
        this.dataLife = 0L;
    }
    
    public EntityCache(long dataLife){
        this.dataLife = dataLife;
    }
    
    public void setDataLife(long milliseconds) {
        this.dataLife = milliseconds;
    }
    
    public void put(String key, T entity){
        this.dataMap.put(key, new CacheStore<T>(entity));
    }
    
    public void remove(String key){
        this.dataMap.remove(key);
    }
    
    public T get(String key) {
        CacheStore<T> cacheStore = this.dataMap.get(key);
        
        if(cacheStore == null)
            return null;
        
        if((new Date().getTime() - cacheStore.getAddDate().getTime()) >= this.dataLife) {
            this.dataMap.remove(key);
            return null;
        }
        
        return cacheStore.getData();
    }

}
