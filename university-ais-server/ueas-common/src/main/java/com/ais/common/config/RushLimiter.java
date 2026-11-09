package com.ais.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * 抢课限流器。
 *
 * <p>基于 Redis INCR + EXPIRE 实现滑动窗口限流，
 * 防止同一学生在抢课期间短时间内发出过多请求。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RushLimiter {

    private final StringRedisTemplate redisTemplate;
    private final RushConfig rushConfig;
    private final DefaultRedisScript<Long> rateLimitScript;

    /**
     * 检查学生是否可以在当前窗口内发起选课请求。
     *
     * @param studentId 学生ID
     * @return true=允许，false=触发限流
     */
    public boolean tryAcquire(Long studentId) {
        String key = rushConfig.rateLimitKey(studentId);
        try {
            Long result = redisTemplate.execute(
                    rateLimitScript,
                    java.util.Collections.singletonList(key),
                    String.valueOf(rushConfig.getRateLimitPerSecond()),
                    String.valueOf(rushConfig.getRateLimitWindowSeconds())
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            // 限流器故障时降级放行，避免影响正常选课
            log.warn("限流器异常，降级放行: studentId={}", studentId, e);
            return true;
        }
    }
}
