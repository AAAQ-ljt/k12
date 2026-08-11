package com.easymall.redis;

import io.lettuce.core.RedisConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedissonConfig {

    @Value("${spring.data.redis.host:}")
    private String redisHost;

    @Value("${spring.data.redis.port:}")
    private Integer redisPort;

    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        try {
            // 创建配置 指定redis地址及节点信息
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
            // 根据config创建出RedissonClient实例
            RedissonClient redissonClient = Redisson.create(config);
            return redissonClient;
        } catch (RedisConnectionException e) {
            log.error("redis配置错误，请检查redis配置");
        }
        return null;
    }
}
