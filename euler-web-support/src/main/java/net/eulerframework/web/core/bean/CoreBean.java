package net.eulerframework.web.core.bean;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.web.config.RedisType;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.i18n.ClassPathReloadableResourceBundleMessageSource;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class CoreBean {
    
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
        messageSource.setCacheSeconds(WebConfig.getI18nRefreshFreq());
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setBasename("classpath*:language/**/*");
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() throws ClassNotFoundException {
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
        PathResource resource = new PathResource(WebConfig.getConfigPath());
        propertyPlaceholderConfigurer.setLocation(resource);
        return propertyPlaceholderConfigurer;
    }

    // @Bean(name="jaxb2Marshaller")
    // public Jaxb2Marshaller jaxb2Marshaller() {
    // Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    // jaxb2Marshaller.setPackagesToScan("net.eulerframework.**.entity",
    // "net.eulerframework.web.core.base.response");
    // return jaxb2Marshaller;
    // }

    @Bean
    public JedisPoolConfig getJedisPoolConfig() {
        //TODO: 改为可外部配置
        JedisPoolConfig JedisPoolConfig = new JedisPoolConfig();
        JedisPoolConfig.setMaxIdle(1000);
        JedisPoolConfig.setMaxTotal(100);
        JedisPoolConfig.setMinIdle(100);
        return JedisPoolConfig;
    }
    
    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        if(!RedisType.STANDALONE.equals(WebConfig.getRedisType())) {
            return null;
        }
        
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(0);
        redisStandaloneConfiguration.setHostName(WebConfig.getRedisHost());
        redisStandaloneConfiguration.setPassword(RedisPassword.of(WebConfig.getRedisPassword()));
        redisStandaloneConfiguration.setPort(WebConfig.getRedisPort());
        return redisStandaloneConfiguration;
    }
    
    @Bean
    public RedisSentinelConfiguration redisSentinelConfiguration() {
        if(!RedisType.SENTINEL.equals(WebConfig.getRedisType())) {
            return null;
        }
        
        //TODO: 完成哨兵Bean
        
        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.setDatabase(0);
        
        String[] sentinelsStr = WebConfig.getRedisSentinels();
        
        for(String sentinelStr : sentinelsStr) {
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
    public JedisConnectionFactory jedisConnectionFactory(
            @Nullable RedisStandaloneConfiguration redisStandaloneConfiguration, 
            @Nullable RedisSentinelConfiguration redisSentinelConfiguration,
            @Nullable JedisPoolConfig jedisPoolConfig) {
        if(redisStandaloneConfiguration != null) {
            return new JedisConnectionFactory(redisStandaloneConfiguration);
        } else if(redisSentinelConfiguration != null) {
            return new JedisConnectionFactory(redisSentinelConfiguration, jedisPoolConfig);
        } else {
            throw new RuntimeException("redis type error");
        }
    }
    
    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(
            JedisConnectionFactory jedisConnectionFactory,
            StringRedisSerializer stringRedisSerializer) {
        StringRedisTemplate stringRedisTemplate= new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(jedisConnectionFactory);
        return stringRedisTemplate;
    }
}
