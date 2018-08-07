/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
