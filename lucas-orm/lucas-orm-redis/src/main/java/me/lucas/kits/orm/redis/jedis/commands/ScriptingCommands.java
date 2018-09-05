package me.lucas.kits.orm.redis.jedis.commands;

/**
 * Created by zhangxin on 2018/9/5-上午11:19.
 *
 * @author zhangxin
 * @version 1.0
 */
public interface ScriptingCommands extends redis.clients.jedis.ScriptingCommands {

    enum RefreshType {
        /** 查询时存入缓存 */SELECT,
        /** 更新时存入缓存 */UPDATE
    }

    enum ResultType {
        /** 成功: 缓存不存在, 缓存刷入 */SUCCESS_SAVE,
        /** 成功: 缓存存在, 比较缓存不存在, 缓存更新 */SUCCESS_REFRESH,
        /** 成功: 缓存存在, 比较缓存存在, 当前版本超前, 缓存更新 */SUCCESS_NEWER_REFRESH,
        /** 失败: 缓存存在, 比较缓存存在, 当前版本落后, 缓存未更新 */UNSUCCESS_OLDER,
        /** 失败: 缓存存在, 比较缓存存在, 当前非UPDATE, 缓存不能更新 */UNSUCCESS_CAN_NOT,
        /** 失败: 更新时失败 */UNSUCCESS_ERROR,
    }

    /**
     * 刷入缓存.
     * 刷新类型使用 {@link RefreshType#SELECT}
     *
     * @param key   Redis Key
     * @param value Redis Value
     * @param ttl   缓存过期时间
     * @return ResultType
     */
    ResultType refreshCache(String key, String value, long ttl);

    /**
     * 刷入缓存.
     * 指定刷新类型
     *
     * @param key         Redis Key
     * @param value       Redis Value
     * @param ttl         缓存过期时间
     * @param refreshType 刷新类型
     * @return ResultType
     */
    ResultType refreshCache(String key, String value, long ttl, RefreshType refreshType);

    /**
     * 刷入缓存.
     *
     * @param key         Redis Key
     * @param value       Redis Value
     * @param ttl         缓存过期时间
     * @param refreshType 刷新类型
     * @param timestamp   操作时间, 用于比较缓存
     * @param refreshTtl  比较缓存过期时间, 默认60s
     * @return ResultType
     */
    ResultType refreshCache(String key, String value, long ttl, RefreshType refreshType, String timestamp,
            long refreshTtl);

}