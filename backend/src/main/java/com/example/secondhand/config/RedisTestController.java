package com.example.secondhand.config;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;

    public RedisTestController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/api/redis/ping")
    public String pingRedis() {
        redisTemplate.opsForValue().set("secondhand:test", "redis-ok", Duration.ofMinutes(1));
        String value = redisTemplate.opsForValue().get("secondhand:test");
        return "Redis connected: " + value;
    }
}