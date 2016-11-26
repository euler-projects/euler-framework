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
package net.eulerframework.web.core.cache;

import net.eulerframework.common.util.SleepTestTool;
import net.eulerframework.web.core.cache.CacheTimerObjectCache.CacheTimer;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by cFrost on 16/10/17.
 */
public class ObjectCachePool {

    private final static Set<AbstractObjectCache> CACHE_POOL = new HashSet<>();

    public static void clean() {
        for(AbstractObjectCache cache : CACHE_POOL) {
            if(cache.isEnable())
                cache.clean();
        }
    }

    public static void add(AbstractObjectCache cache){
        CACHE_POOL.add(cache);
    }

    public static void remove(AbstractObjectCache cache){
        CACHE_POOL.remove(cache);
    }

    public static <KEY_T, DATA_T> DefaultObjectCache<KEY_T, DATA_T> generateDefaultObjectCache(long dataLife) {

        DefaultObjectCache<KEY_T, DATA_T> newCache = new DefaultObjectCache<>(dataLife);
        CACHE_POOL.add(newCache);

        return newCache;
    }

    public static <KEY_T, DATA_T> CacheTimerObjectCache<KEY_T, DATA_T> generateCacheTimerObjectCache(CacheTimer cacheTimer) {

        CacheTimerObjectCache<KEY_T, DATA_T> newCache = new CacheTimerObjectCache<>(cacheTimer);
        CACHE_POOL.add(newCache);

        return newCache;
    }

    public static void main(String[] args) {
        DefaultObjectCache<Integer, String> cache = ObjectCachePool.generateDefaultObjectCache(10000);
        DefaultObjectCache<Integer, String> cache3 = new DefaultObjectCache(20000);
        ObjectCachePool.add(cache3);

        CacheTimer timer = new CacheTimer<String>() {

            @Override
            public boolean isTimeout(String data, long addTime) {
                if(new Date().getTime() - addTime > 5000){
                    return true;
                }
                return false;
            }
        };

        CacheTimerObjectCache<Integer, String> cache2 = ObjectCachePool.generateCacheTimerObjectCache(timer);
        for(int i = 0 ; i < 2 ; i++) {
            SleepTestTool.sleep(10);
            cache.put(i, String.valueOf(i));
            cache2.put(i, String.valueOf(i));
            cache3.put(i, String.valueOf(i));
        }
        while(true) {
            ObjectCachePool.clean();
        }
    }
}
