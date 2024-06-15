/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.core.bean;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.eulerframework.config.EulerWebSupportConfig;
import org.eulerframework.web.config.RedisType;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.context.support.ClassPathReloadableResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.beans.PropertyEditorSupport;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Configuration
public class CoreBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ControllerAdvice
    public static class GlobalParameterBinder {
        /**
         * 尝试以时间戳的方式格式化时间,如果失败则传递原始字符串
         *
         * @param binder
         */
        @InitBinder
        public void initBinder(WebDataBinder binder) {
            binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
                @Override
                public void setAsText(String value) {
                    if (StringUtils.hasText(value)) {
                        try {
                            long timestamp = Long.parseLong(value);
                            setValue(new Date(timestamp));
                        } catch (NumberFormatException e) {
                            setValue(value);
                        }
                    } else {
                        setValue(value);
                    }
                }
            });
        }
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public MessageSource messageSource() {
        ClassPathReloadableResourceBundleMessageSource messageSource = new ClassPathReloadableResourceBundleMessageSource();
        //messageSource.setCacheSeconds(default as -1);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setBasename("classpath*:language/**/*");
        return messageSource;
    }

    @Bean
    public Validator localValidatorFactoryBean() throws ClassNotFoundException {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setProviderClass(Class.forName("org.hibernate.validator.HibernateValidator"));
        validator.setValidationMessageSource(this.messageSource());
        return validator;
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() throws ClassNotFoundException {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(this.localValidatorFactoryBean());
        return processor;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver();
    }

    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }

    @Bean(name = "secretPropertyConfigurer")
    public PropertyPlaceholderConfigurer secretPropertyConfigurer() {
        PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
        propertyPlaceholderConfigurer.setFileEncoding("utf-8");
        propertyPlaceholderConfigurer.setOrder(1);
        propertyPlaceholderConfigurer.setIgnoreResourceNotFound(true);
        propertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        FileSystemResource resource = new FileSystemResource(WebConfig.getAdditionalConfigPath() + WebConfig.DEFAULT_CONFIG_FILE);
        propertyPlaceholderConfigurer.setLocation(resource);
        return propertyPlaceholderConfigurer;
    }

    // @Bean(name="jaxb2Marshaller")
    // public Jaxb2Marshaller jaxb2Marshaller() {
    // Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    // jaxb2Marshaller.setPackagesToScan("org.eulerframework.**.entity",
    // "org.eulerframework.web.core.base.response");
    // return jaxb2Marshaller;
    // }

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        if (!RedisType.STANDALONE.equals(EulerWebSupportConfig.getRedisType())) {
            return null;
        }

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(0);
        redisStandaloneConfiguration.setHostName(EulerWebSupportConfig.getRedisHost());
        redisStandaloneConfiguration.setPassword(RedisPassword.of(EulerWebSupportConfig.getRedisPassword()));
        redisStandaloneConfiguration.setPort(EulerWebSupportConfig.getRedisPort());
        return redisStandaloneConfiguration;
    }

    @Bean
    public RedisSentinelConfiguration redisSentinelConfiguration() {
        if (!RedisType.SENTINEL.equals(EulerWebSupportConfig.getRedisType())) {
            return null;
        }

        //TODO: 完成哨兵Bean

        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.setDatabase(0);

        String[] sentinelsStr = EulerWebSupportConfig.getRedisSentinels();

        for (String sentinelStr : sentinelsStr) {
            String[] sentinelStrArray = sentinelStr.split(":");
            Assert.isTrue(sentinelStrArray.length == 2, "sentinel format must be host:port");
            String host = sentinelStrArray[0];
            int port = Integer.parseInt(sentinelStrArray[1]);
            RedisNode sentinel = new RedisNode(host, port);
            redisSentinelConfiguration.addSentinel(sentinel);
        }

        return redisSentinelConfiguration;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @Nullable RedisStandaloneConfiguration redisStandaloneConfiguration,
            @Nullable RedisSentinelConfiguration redisSentinelConfiguration) {
        if (redisStandaloneConfiguration != null) {
            if (this.isLettuce()) {
                this.logger.info("Lettuce Standalone");
                return new LettuceConnectionFactory(redisStandaloneConfiguration);
            } else if (this.isJedis()) {
                this.logger.info("Jedis Standalone");
                return new JedisConnectionFactory(redisStandaloneConfiguration);
            } else {
                throw new RuntimeException("Jedis or Lettuce not exits.");
            }
        } else if (redisSentinelConfiguration != null) {
            if (this.isLettuce()) {
                this.logger.info("Lettuce Sentinel");
                return new LettuceConnectionFactory(redisSentinelConfiguration);
            } else if (this.isJedis()) {
                this.logger.info("Jedis Sentinel");
                return new JedisConnectionFactory(redisSentinelConfiguration);
            } else {
                throw new RuntimeException("Jedis or Lettuce not exits.");
            }
        } else {
            throw new RuntimeException("redis type error");
        }
    }

    private boolean isLettuce() {
        try {
            Class.forName("io.lettuce.core.RedisClient");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isJedis() {
        try {
            Class.forName("redis.clients.jedis.Jedis");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisSerializer stringRedisSerializer) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }
}
