package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean//当在一个方法上标注 @Bean 时，Spring 会自动调用该方法，并将方法的返回值注册为 Spring 容器中的一个 Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建Redis模板对象...");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置Redis连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置Redis的key序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
