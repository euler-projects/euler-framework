/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 cFrost.sun (SUN BIN) 
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
 * For more information, please visit the following websites
 * 
 * https://eulerproject.io/euler-framework
 * https://cfrost.net
 */
package net.eulerframework.redis;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.common.util.JavaObjectUtils;

/**
 * @author cFrost
 *
 */
public abstract class AbstractStringKeyRedisTemplate<T> extends RedisTemplate<String, T> implements InitializingBean {

    @Autowired
    private RedisConnectionFactory connectionFactory;
    
    @Autowired
    private StringRedisSerializer stringRedisSerialize;

    @Autowired
    private ObjectMapper objectMapper;
    
    protected Jackson2JsonRedisSerializer<T> jackson2JsonRedisSerializer;
    
    @SuppressWarnings("unchecked")
    public AbstractStringKeyRedisTemplate() {
        this.jackson2JsonRedisSerializer = 
                new Jackson2JsonRedisSerializer<T>((Class<T>) JavaObjectUtils.findSuperClassGenricType(this.getClass(), 0));
    }
    
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.connectionFactory, "connectionFactory can not be null");
        Assert.notNull(this.stringRedisSerialize, "stringRedisSerialize can not be null");
        Assert.notNull(this.objectMapper, "objectMapper can not be null");
        Assert.notNull(this.jackson2JsonRedisSerializer, "jackson2JsonRedisSerializer can not be null");
        
        this.jackson2JsonRedisSerializer.setObjectMapper(this.objectMapper);
        
        super.setKeySerializer(stringRedisSerialize);
        super.setValueSerializer(this.jackson2JsonRedisSerializer);
        super.setConnectionFactory(this.connectionFactory);
        super.afterPropertiesSet();
    }

}
