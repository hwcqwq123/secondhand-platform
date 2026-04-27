package com.example.secondhand.order;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取 Redis 锁。
     *
     * key：锁名称，例如 lock:item:6
     * ttl：锁自动过期时间，防止服务异常导致死锁
     *
     * 返回：
     * token != null：获取锁成功
     * token == null：获取锁失败
     */
    public String tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);

        if (Boolean.TRUE.equals(success)) {
            return token;
        }

        return null;
    }

    /**
     * 释放锁。
     *
     * 使用 Lua 脚本保证：
     * 只有持有当前 token 的请求才能删除锁。
     *
     * 避免 A 请求的锁过期后，B 请求拿到新锁，
     * A 请求执行 unlock 时误删 B 请求的锁。
     */
    public void unlock(String key, String token) {
        if (key == null || token == null) {
            return;
        }

        String lua = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(lua);
        script.setResultType(Long.class);

        redisTemplate.execute(script, Collections.singletonList(key), token);
    }
}