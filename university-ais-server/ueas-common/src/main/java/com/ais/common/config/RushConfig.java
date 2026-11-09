package com.ais.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 抢课高并发防护配置。
 *
 * <p>提供抢课场景下的限流和防重机制：
 * <ul>
 *   <li>单用户限流：抢课期间每个学生每秒最多提交 N 次（防脚本刷接口）</li>
 *   <li>抢课时段控制：管理员可通过 API 设置抢课开始/结束时间窗口</li>
 * </ul>
 */
@Slf4j
@Configuration
public class RushConfig {

    /** 抢课开关 Redis key */
    public static final String ENROLLMENT_ENABLED_KEY = "sys:config:enrollment_enabled";

    /** 抢课开始时间 Redis key（epoch millis） */
    public static final String ENROLLMENT_START_KEY = "sys:config:enrollment_start";

    /** 抢课结束时间 Redis key（epoch millis） */
    public static final String ENROLLMENT_END_KEY = "sys:config:enrollment_end";

    /** 限流 key 前缀 */
    public static final String RATE_LIMIT_KEY_PREFIX = "rush:limit:";

    /** 单用户每秒最大选课请求次数 */
    @Value("${rush.rate-limit.per-second:3}")
    private int rateLimitPerSecond;

    /** 限流窗口（秒） */
    @Value("${rush.rate-limit.window-seconds:1}")
    private int rateLimitWindowSeconds;

    /**
     * Lua脚本：原子地检查并增加限流计数。
     * 返回：1=允许，0=拒绝（已达上限）
     *
     * KEYS[1] = 限流key
     * ARGV[1] = 窗口秒数
     * ARGV[2] = 上限次数
     */
    @Bean
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(
                "local count = redis.call('INCR', KEYS[1])\n" +
                "if count == 1 then\n" +
                "    redis.call('EXPIRE', KEYS[1], ARGV[2])\n" +
                "end\n" +
                "if count > tonumber(ARGV[1]) then\n" +
                "    return 0\n" +
                "end\n" +
                "return 1"
        );
        script.setResultType(Long.class);
        return script;
    }

    /**
     * 判断当前是否在抢课时段内。
     * 未设置时段时默认允许（向后兼容）。
     */
    public boolean isInRushWindow(StringRedisTemplate redisTemplate) {
        String startTimeStr = redisTemplate.opsForValue().get(ENROLLMENT_START_KEY);
        String endTimeStr = redisTemplate.opsForValue().get(ENROLLMENT_END_KEY);

        if (startTimeStr == null || endTimeStr == null) {
            return true; // 未设置时段，允许抢课
        }

        try {
            long now = System.currentTimeMillis();
            long start = Long.parseLong(startTimeStr);
            long end = Long.parseLong(endTimeStr);
            return now >= start && now <= end;
        } catch (NumberFormatException e) {
            log.warn("抢课时段解析失败: start={}, end={}", startTimeStr, endTimeStr);
            return true;
        }
    }

    /**
     * 获取抢课开关状态。
     */
    public boolean isEnrollmentEnabled(StringRedisTemplate redisTemplate) {
        String val = redisTemplate.opsForValue().get(ENROLLMENT_ENABLED_KEY);
        return "true".equals(val);
    }

    /**
     * 构造限流key。
     */
    public String rateLimitKey(Long studentId) {
        return RATE_LIMIT_KEY_PREFIX + studentId;
    }

    public int getRateLimitPerSecond() {
        return rateLimitPerSecond;
    }

    public int getRateLimitWindowSeconds() {
        return rateLimitWindowSeconds;
    }

    public String getEnrollmentStartKey() {
        return ENROLLMENT_START_KEY;
    }

    public String getEnrollmentEndKey() {
        return ENROLLMENT_END_KEY;
    }
}
