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
 * http://eulerframework.net
 * http://cfrost.net
 */
package net.eulerframework.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cFrost on 16/10/17.
 */
public abstract class AbstractObjectCache<KEY_T, DATA_T> {
    protected Logger logger = LogManager.getLogger(this.getClass());

    protected final HashMap<KEY_T, DataStore<DATA_T>> dataMap = new HashMap<>();

    protected ReentrantLock cacheWriteLock = new ReentrantLock();

    /**
     * 向缓存添加缓存对象<br>
     * 如果缓存已被其他线程锁定,则放弃添加,返回<code>false</code>
     * @param key 缓存索引键值
     * @param data 缓存对象
     * @return 成功返回<code>true</code>;失败返回<code>false</code>
     */
    public boolean put(KEY_T key, DATA_T data) {
        if(!this.isEnable())
            return false;

        if(this.cacheWriteLock.tryLock()) {
            try {
                this.dataMap.put(key, new DataStore<DATA_T>(data));
                return true;
            } finally {
                this.cacheWriteLock.unlock();
            }
        }

        return false;
    }

    /**
     * 删除缓存对象<br>
     * 如果缓存已被其他线程锁定,则放弃删除,返回<code>false</code>
     * @param key 缓存索引键值
     * @return 成功返回<code>true</code>;失败返回<code>false</code>
     */
    public boolean remove(KEY_T key) {
        if(this.cacheWriteLock.tryLock()) {
            try {
                this.dataMap.remove(key);
                return true;
            } finally {
                this.cacheWriteLock.unlock();
            }
        }
        return false;
    }

    /**
     * 清除所有缓存对象<br>
     * 如果缓存已被其他线程锁定,则放弃清除,返回<code>false</code>
     * @return 成功返回<code>true</code>;失败返回<code>false</code>
     */
    public boolean clear() {
        if(this.cacheWriteLock.tryLock()) {
            try {
                this.dataMap.clear();
                return true;
            } finally {
                this.cacheWriteLock.unlock();
            }
        }
        return false;
    }

    /**
     * 清理缓存<br>
     * 尝试删除所有过期缓存对象
     */
    public void clean() {
        Set<KEY_T> keySet = this.dataMap.keySet();
        Set<KEY_T> keySetNeedRemove = new HashSet<>();
        for(KEY_T key : keySet) {
            DataStore<DATA_T> storedData = this.dataMap.get(key);

            if(this.isTimeout(storedData)) {
                keySetNeedRemove.add(key);

                this.logger.info("Data key = " + key + " was time out and will be removed.");

            }
        }

        for(KEY_T key : keySetNeedRemove) {
            this.remove(key);
        }
    }

    /**
     * 查询缓存对象
     * @param key 缓存索引键值
     * @return 缓存对象,未查到或过期返回<code>null</code>
     */
    public DATA_T get(KEY_T key) {
        if(!this.isEnable())
            return null;

        DataStore<DATA_T> storedData = this.dataMap.get(key);

        if(storedData == null)
            return null;

        if(this.isTimeout(storedData)) {
            this.remove(key);
            return null;
        }

        return storedData.getData();
    }

    /**
     * 判断缓存对象是否过期
     * @param storedData
     * @return
     */
    public abstract boolean isTimeout(DataStore<DATA_T> storedData);

    /**
     * 判断缓存是否启用
     * @return
     */
    public abstract boolean isEnable();
}
