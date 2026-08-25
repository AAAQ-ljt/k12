package com.nexora.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 百炼文生图分布式限流器（Redis 令牌桶）。
 * 阿里百炼 qwen-image 按主账号维度约 20 次/分钟限流，这里把速率放低到约 12 次/分钟，
 * 并禁止突发，避免绘本 3 路并发和重试把额度瞬间打满。
 */
@Slf4j
@Component
public class DashscopeRateLimiter {

    private static final String KEY_PREFIX = "nexora:image:rate:dashscope:";
    private static final double CAPACITY = 1;
    private static final double PERMITS_PER_SECOND = 0.2;
    private static final long ACQUIRE_POLL_MS = 500;
    private static final String ACQUIRE_SCRIPT_TEXT = """
            local tokens = tonumber(redis.call('HGET', KEYS[1], 'tokens'))
            local last = tonumber(redis.call('HGET', KEYS[1], 'last'))
            local capacity = tonumber(ARGV[1])
            local rate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            if tokens == nil then tokens = capacity end
            if last == nil then last = now end
            local elapsed = (now - last) / 1000.0
            if elapsed < 0 then elapsed = 0 end
            tokens = math.min(capacity, tokens + elapsed * rate)
            if tokens < 1 then
                return 0
            end
            redis.call('HSET', KEYS[1], 'tokens', tokens - 1, 'last', now)
            redis.call('PEXPIRE', KEYS[1], 60000)
            return 1
            """;
    private static final RedisScript<Long> ACQUIRE_SCRIPT =
            new DefaultRedisScript<>(ACQUIRE_SCRIPT_TEXT, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    public DashscopeRateLimiter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 尝试领取一次调用额度；Redis 不可用时放行（避免限流器故障阻断生图）。
     */
    public boolean tryAcquireDashscope(String model) {
        String key = KEY_PREFIX + (model == null || model.isBlank() ? "default" : model);
        try {
            Long result = stringRedisTemplate.execute(
                    ACQUIRE_SCRIPT,
                    List.of(key),
                    String.valueOf(CAPACITY),
                    String.valueOf(PERMITS_PER_SECOND),
                    String.valueOf(System.currentTimeMillis()));
            return result != null && result == 1L;
        } catch (Exception e) {
            log.warn("Redis 文生图限流不可用，暂时放行: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 阻塞领取额度，最多等待 timeoutMs。
     */
    public boolean acquireDashscope(String model, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (true) {
            if (tryAcquireDashscope(model)) {
                return true;
            }
            if (System.currentTimeMillis() >= deadline) {
                return false;
            }
            try {
                Thread.sleep(ACQUIRE_POLL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
}
