package com.ais.system.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Sa-Token Redis 数据访问对象实现。
 *
 * <p>使用 Spring RedisTemplate 实现 SaTokenDao 接口，将 Token 和 Session
 * 存储到 Redis 中，支持分布式部署和 Token 持久化。
 */
@Component
public class SaTokenDaoRedisImpl implements SaTokenDao {

    /** Redis Key 前缀 */
    private static final String KEY_TOKEN_PREFIX = "sa-token:token:";
    private static final String KEY_SESSION_PREFIX = "sa-token:session:";
    private static final String KEY_STP_PREFIX = "sa-token:stp:";

    /** 永久不过期 */
    public static final long NEVER_EXPIRE = SaTokenDao.NEVER_EXPIRE;

    /** 不存在或已过期 */
    public static final long NOT_VALUE_EXPIRE = SaTokenDao.NOT_VALUE_EXPIRE;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取 Value，没有则返回 null。
     */
    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 写入 Value，并设置存活时间（秒）。
     * 如果 timeout == NEVER_EXPIRE，则永不过期。
     */
    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == NEVER_EXPIRE) {
            redisTemplate.opsForValue().set(key, value);
        } else if (timeout > 0) {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeout));
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 更新 Value（不改变存活时间）。
     */
    @Override
    public void update(String key, String value) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl != null && ttl > 0) {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttl));
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 删除 Value。
     */
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 获取存活时间（秒），不存在返回 NOT_VALUE_EXPIRE。
     */
    @Override
    public long getTimeout(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null && ttl >= 0 ? ttl : NOT_VALUE_EXPIRE;
    }

    /**
     * 更新存活时间（秒）。
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        if (timeout == NEVER_EXPIRE) {
            // 保持已有 TTL 不变
            return;
        }
        String value = get(key);
        if (value != null) {
            set(key, value, timeout);
        }
    }

    /**
     * 获取 Object，没有则返回 null。
     * <p>如果 JSON 数据包含 SaSession 特有的字段（id、type、loginType），
     * 则将其还原为 SaSession 对象，否则使用通用反序列化。
     */
    @Override
    public Object getObject(String key) {
        String json = get(key);
        if (json == null) {
            return null;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            // 检测是否为 SaSession 数据，如果是则构建 SaSession 实例
            if (map.containsKey("id") && map.containsKey("type") && map.containsKey("loginType")) {
                return buildSaSession(json, map);
            }
            return objectMapper.convertValue(map, Object.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 JSON 和 Map 数据构建 SaSession 对象。
     */
    @SuppressWarnings("unchecked")
    private SaSession buildSaSession(String json, Map<String, Object> map) {
        try {
            SaSession session = new SaSession();
            session.setId((String) map.get("id"));
            session.setType((String) map.get("type"));
            session.setLoginType((String) map.get("loginType"));
            session.setLoginId(map.get("loginId"));
            session.setToken((String) map.get("token"));
            session.setCreateTime(((Number) map.get("createTime")).longValue());
            // 还原 dataMap
            Object dataMapObj = map.get("dataMap");
            if (dataMapObj instanceof Map) {
                session.refreshDataMap((Map<String, Object>) dataMapObj);
            }
            // 还原 tokenSignList
            Object signListObj = map.get("tokenSignList");
            if (signListObj instanceof List) {
                List<Object> signList = (List<Object>) signListObj;
                List<cn.dev33.satoken.session.TokenSign> signs = new ArrayList<>();
                for (Object item : signList) {
                    if (item instanceof Map) {
                        Map<String, Object> signMap = (Map<String, Object>) item;
                        cn.dev33.satoken.session.TokenSign sign = new cn.dev33.satoken.session.TokenSign();
                        sign.setValue((String) signMap.get("value"));
                        sign.setDevice((String) signMap.get("device"));
                        if (signMap.containsKey("tag")) {
                            sign.setTag(signMap.get("tag"));
                        }
                        signs.add(sign);
                    }
                }
                session.setTokenSignList(signs);
            }
            return session;
        } catch (Exception e) {
            // 构建失败则回退到原始 Map
            return null;
        }
    }

    /**
     * 写入 Object，并设置存活时间（秒）。
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        try {
            String json = objectMapper.writeValueAsString(object);
            set(key, json, timeout);
        } catch (Exception e) {
            // 序列化失败时忽略
        }
    }

    /**
     * 更新 Object（不改变存活时间）。
     */
    @Override
    public void updateObject(String key, Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            update(key, json);
        } catch (Exception e) {
            // 序列化失败时忽略
        }
    }

    /**
     * 删除 Object。
     */
    @Override
    public void deleteObject(String key) {
        delete(key);
    }

    /**
     * 获取 Object 的存活时间（秒）。
     */
    @Override
    public long getObjectTimeout(String key) {
        return getTimeout(key);
    }

    /**
     * 更新 Object 的存活时间（秒）。
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        updateTimeout(key, timeout);
    }

    /**
     * 获取 Session 对象。
     */
    @Override
    public SaSession getSession(String sessionId) {
        Object obj = getObject(KEY_SESSION_PREFIX + sessionId);
        if (obj instanceof SaSession) {
            return (SaSession) obj;
        }
        return null;
    }

    /**
     * 写入 Session。
     */
    @Override
    public void setSession(SaSession session, long timeout) {
        setObject(KEY_SESSION_PREFIX + session.getId(), session, timeout);
    }

    /**
     * 更新 Session。
     */
    @Override
    public void updateSession(SaSession session) {
        updateObject(KEY_SESSION_PREFIX + session.getId(), session);
    }

    /**
     * 删除 Session。
     */
    @Override
    public void deleteSession(String sessionId) {
        deleteObject(KEY_SESSION_PREFIX + sessionId);
    }

    /**
     * 获取 Session 的存活时间（秒）。
     */
    @Override
    public long getSessionTimeout(String sessionId) {
        return getObjectTimeout(KEY_SESSION_PREFIX + sessionId);
    }

    /**
     * 更新 Session 的存活时间（秒）。
     */
    @Override
    public void updateSessionTimeout(String sessionId, long timeout) {
        updateObjectTimeout(KEY_SESSION_PREFIX + sessionId, timeout);
    }

    /**
     * 搜索数据（用于查找 token 列表等）。
     */
    @Override
    public List<String> searchData(String keyPrefix, String keyword, int start, int size, boolean isPattern) {
        List<String> result = new ArrayList<>();
        String searchKey = isPattern
                ? keyPrefix + keyword + "*"
                : keyPrefix + keyword;
        Set<String> keys = redisTemplate.keys(searchKey);
        if (keys != null) {
            int count = 0;
            for (String key : keys) {
                if (start > 0) {
                    start--;
                    continue;
                }
                if (size > 0 && count >= size) {
                    break;
                }
                result.add(key.substring(keyPrefix.length()));
                count++;
            }
        }
        return result;
    }
}
