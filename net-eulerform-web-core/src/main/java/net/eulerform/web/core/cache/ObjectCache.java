/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015-2016 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://github.com/euler-form/web-form
 * http://eulerform.net
 * http://cfrost.net
 */
package net.eulerform.web.core.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 非阻塞对象缓存,可通过索引键值查询缓存对象,
 * 在缓存被其他线程修改时,本线程对缓存的修改会被忽略,而不会阻塞程序运行<br>
 * 需要设定数据生命周期,单位:秒. 默认为<code>0</code>,即缓存不启用
 * <strong>注意:</strong>查出的数据应<strong>避免</strong>被修改
 * 
 * @author cFrost
 *
 * @param <KEY_T> 缓存索引数据类型
 * @param <DATA_T> 缓存对象类型
 */
public class ObjectCache<KEY_T, DATA_T> {
    private final HashMap<KEY_T, DataStore<DATA_T>> dataMap = new HashMap<>();

    private long dataLife;

    private ReentrantLock cacheWritelock = new ReentrantLock();

    /**
     * 新建缓存对象,默认数据生命周期为0s
     */
    public ObjectCache() {
        this.dataLife = 0L;
    }

    /**
     * 新建缓存对象并指定数据生命周期
     * @param dataLife 数据生命周期,单位:毫秒,设为<code>&lt;=0</code>的值表示禁用缓存
     */
    public ObjectCache(long dataLife) {
        this.dataLife = dataLife;
    }

    /**
     * 更改数据生命周期
     * @param milliseconds 数据生命周期,单位:毫秒,设为<code>&lt;=0</code>的值表示禁用缓存
     */
    public void setDataLife(long milliseconds) {
        this.dataLife = milliseconds;
    }

    /**
     * 向缓存添加缓存对象<br>
     * 如果添加成功,返回<code>true</code><br>
     * 如果缓存已被其他线程锁定,则放弃添加,返回<code>false</code><br>
     * 如果数据生命周期被设为<code>&lt;=0</code>的值,则放弃添加,返回<code>false</code><br>
     * @param key 缓存索引键值
     * @param data 缓存对象
     * @return
     */
    public boolean put(KEY_T key, DATA_T data) {
        if(this.dataLife <= 0)
            return false;
        
        if (cacheWritelock.tryLock()) {
            try {
                this.dataMap.put(key, new DataStore<DATA_T>(data));
                return true;
            } finally {
                cacheWritelock.unlock();
            }
        }
        return false;
    }

    /**
     * 根据索引键值删除缓存对象<br>
     * 如果删除成功,返回<code>true</code><br>
     * 如果缓存已被其他线程锁定,则放弃删除,返回<code>false</code><br>
     * @param key 缓存索引键值
     * @return <code>true</code>表示删除成功,<code>false</code>表示删除失败
     */
    public boolean remove(KEY_T key) {
        if (cacheWritelock.tryLock()) {
            try {
                this.dataMap.remove(key);
                return true;
            } finally {
                cacheWritelock.unlock();
            }
        }
        return false;
    }

    /**
     * 根据索引键值查找缓存对象<br>
     * 如果未查到或缓存对象过期返回<code>null</code>
     * @param key 缓存索引键值
     * @return 缓存对象
     */
    public DATA_T get(KEY_T key) {
        if(this.dataLife <= 0)
            return null;
        
        DataStore<DATA_T> cacheStore = this.dataMap.get(key);

        if (cacheStore == null)
            return null;

        if ((new Date().getTime() - cacheStore.getAddTime()) >= this.dataLife) {
            remove(key);
            return null;
        }

        return cacheStore.getData();
    }

}
