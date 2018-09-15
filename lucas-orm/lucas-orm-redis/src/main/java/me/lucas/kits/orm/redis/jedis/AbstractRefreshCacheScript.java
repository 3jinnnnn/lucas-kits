package me.lucas.kits.orm.redis.jedis;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.commons.utils.Charsets;

/**
 * Created by zhangxin on 2018/9/15-上午11:25.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
public abstract class AbstractRefreshCacheScript implements RedisClient {
    public static String REFRESH_CACHE_SCRIPT;
    public static String REFRESH_HASH_CACHE_SCRIPT;

    static {
        REFRESH_CACHE_SCRIPT = readRefreshCacheLua("RefreshCache.lua");
        REFRESH_HASH_CACHE_SCRIPT = readRefreshCacheLua("RefreshHashCache.lua");
    }

    private static String readRefreshCacheLua(String fileName) {
        File file = null;
        StringBuilder sb = new StringBuilder();
        try (InputStream is = AbstractRedisClient.class.getClassLoader().getResourceAsStream(fileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));) {
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s).append('\n');
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return sb.toString();
    }

    @Override
    public ResultType refreshCache(String key, String value, long ttl) {
        try {
            Object eval = eval(REFRESH_CACHE_SCRIPT, 1, key, value, String.valueOf(ttl), RefreshType.SELECT.name());
            return ResultType.valueOf(eval.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultType.UNSUCCESS_ERROR;
        }
    }

    @Override
    public ResultType refreshCache(String key, String value, long ttl, RefreshType refreshType) {
        try {
            Object eval = eval(REFRESH_CACHE_SCRIPT, 1, key, value, String.valueOf(ttl), refreshType.name(),
                    String.valueOf(System.currentTimeMillis()), "60");
            return ResultType.valueOf(eval.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultType.UNSUCCESS_ERROR;
        }
    }

    @Override
    public ResultType refreshCache(String key, String value, long ttl, RefreshType refreshType, String timestamp,
            long refreshTtl) {
        try {
            Object eval = eval(REFRESH_CACHE_SCRIPT, 1, key, value, String.valueOf(ttl), refreshType.name(), timestamp,
                    String.valueOf(refreshTtl));
            return ResultType.valueOf(eval.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultType.UNSUCCESS_ERROR;
        }
    }

    @Override
    public ResultType refreshCache(String key, String value, long ttl, RefreshType refreshType, long timestamp,
            long refreshTtl) {
        try {
            Object eval = eval(REFRESH_CACHE_SCRIPT, 1, key, value, String.valueOf(ttl), refreshType.name(),
                    String.valueOf(timestamp), String.valueOf(refreshTtl));
            return ResultType.valueOf(eval.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultType.UNSUCCESS_ERROR;
        }
    }

    @Override
    public ResultType refreshHashCache(String key, String field, String value, long ttl) {
        try {
            Object eval = eval(REFRESH_HASH_CACHE_SCRIPT, 2, key, field, value, String.valueOf(ttl),
                    RefreshType.SELECT.name());
            return ResultType.valueOf(eval.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultType.UNSUCCESS_ERROR;
        }
    }

    @Override
    public ResultType refreshHashCache(String key, String field, String value, long ttl, RefreshType refreshType) {
        try {
            Object eval = eval(REFRESH_HASH_CACHE_SCRIPT, 2, key, field, value, String.valueOf(ttl), refreshType.name(),
                    String.valueOf(System.currentTimeMillis()), "60");
            return ResultType.valueOf(eval.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultType.UNSUCCESS_ERROR;
        }
    }

    @Override
    public ResultType refreshHashCache(String key, String field, String value, long ttl, RefreshType refreshType,
            String timestamp, long refreshTtl) {
        try {
            Object eval = eval(REFRESH_HASH_CACHE_SCRIPT, 2, key, field, value, String.valueOf(ttl), refreshType.name(),
                    timestamp, String.valueOf(refreshTtl));
            return ResultType.valueOf(eval.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultType.UNSUCCESS_ERROR;
        }
    }

    @Override
    public ResultType refreshHashCache(String key, String field, String value, long ttl, RefreshType refreshType,
            long timestamp, long refreshTtl) {
        try {
            Object eval = eval(REFRESH_HASH_CACHE_SCRIPT, 2, key, field, value, String.valueOf(ttl), refreshType.name(),
                    String.valueOf(timestamp), String.valueOf(refreshTtl));
            return ResultType.valueOf(eval.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultType.UNSUCCESS_ERROR;
        }
    }
}
