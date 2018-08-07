packagesToScan("net.eulerframework.**.entity",
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
