/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.lucas.kits.orm.redis.jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import me.lucas.kits.orm.redis.jedis.commands.HashRedisClient;
import me.lucas.kits.orm.redis.jedis.commands.KeyValueRedisClient;
import me.lucas.kits.orm.redis.jedis.commands.ListRedisClient;
import me.lucas.kits.orm.redis.jedis.commands.ScriptingCommands;
import me.lucas.kits.orm.redis.jedis.commands.SetRedisClient;
import me.lucas.kits.orm.redis.jedis.commands.SortedSetRedisClient;
import me.lucas.kits.orm.redis.jedis.config.RedisConfig;

/**
 * 针对Jedis池的使用而基础的封装，主要针对Sharding模式进行接口API的定义<br>
 * 如果Hosts配置为单节点时则无需特别注意，但是配置了多个节点时，
 * 则无法使用部分功能，这些功能主要因分布式而无法保证使用，
 * 但是部分操作可以进行遍历Sharding节点来进行操作，这也是可以满足操作的<br>
 *
 * @author yanghe
 * @since 1.0
 */
public interface RedisClient
        extends KeyValueRedisClient, HashRedisClient, ListRedisClient, SetRedisClient, SortedSetRedisClient,
        ScriptingCommands {
    enum Mark {
        /** 从列表的左端读取元素 */
        LPOP,

        /** 从列表的右端读取元素 */
        RPOP,

        /** 将元素写入列表的左端 */
        LPUSH,

        /** 将元素写入列表的右端 */
        RPUSH,

        /** linsert position on before */
        BEFORE,

        /** linsert position on after */
        AFTER,

        /** PUSH列表时的策略，以KEY为基准 */
        KEY,

        /** PUSH列表时的策略，以VALUE为基准 */
        VALUE;
    }

    RedisConfig getConfig();

    /**
     * @return Info list
     * @since 1.3.15
     */
    List<Map<String, String>> info();

    /**
     * @param section the section
     * @return Info list
     * @since 1.3.15
     */
    List<Map<String, String>> info(String section);

    /**
     * 删除给定的一个或多个 key 。
     *
     * @param keys key动态数组
     * @return 删除key的数量
     */
    long del(String... keys);

    /**
     * 删除给定列表的所有key 。
     *
     * @param keys key列表
     * @return 删除key的数量
     */
    long del(List<String> keys);

    /**
     * 检查给定 key 是否存在。
     *
     * @param key 散列Key
     * @return 如果存在则返回true，否则返回false
     */
    boolean exists(String key);

    /**
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     *
     * @param key     散列Key
     * @param seconds 过期时间，单位：秒
     * @return 设置成功返回 1 。当 key 不存在或者不能为 key 设置生存时间时(比如在低于 2.1.3 版本的 Redis 中你尝试更新 key 的生存时间)，返回 0 。
     */
    long expire(String key, int seconds);

    /**
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     * 使用RedisConfig中的过期时间进行设置
     *
     * @param key 散列key
     * @return 设置成功返回 1 。当 key 不存在或者不能为 key 设置生存时间时(比如在低于 2.1.3 版本的 Redis 中你尝试更新 key 的生存时间)，返回 0 。
     * @see RedisConfig#getExpireTime()
     */
    long expire(String key);

    /**
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     *
     * @param key       散列Key
     * @param timestamp 过期时间，时间戳，自动将毫秒制转换成秒制
     * @return 如果生存时间设置成功，返回 1 。当 key 不存在或没办法设置生存时间，返回 0 。
     */
    long expireat(String key, long timestamp);

    /**
     * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。
     *
     * @param key 元素Key
     * @return 当 key 不存在时，返回 -2 。<br>
     * 当 key 存在但没有设置剩余生存时间时，返回 -1 。<br>
     * 否则，以秒为单位，返回 key 的剩余生存时间。
     */
    long ttl(String key);

    /**
     * 查找所有符合给定模式 pattern 的 key 。
     *
     * @param pattern 匹配规则
     * @return 符合给定模式的 key 列表。
     */
    Set<String> keys(String pattern);

    /**
     * 添加指定元素到 HyperLogLog 中.
     * @param key KEY
     * @param elements 元素
     * @return 成功条数, 批量插入时为1
     */
    Long pfadd(final String key, final String... elements);

    /**
     * 将多个 HyperLogLog 合并为一个 HyperLogLog.
     * @param destkey 目标KEY
     * @param sourcekeys SOURCE KEY
     * @return 是否成功
     */
    boolean pfmerge(final String destkey, final String... sourcekeys);

    /**
     * 返回给定 HyperLogLog 的基数估算值。
     * @param key KEY
     * @return count
     */
    long pfcount(final String key);

    /**
     * 返回给定 HyperLogLog 的基数估算值。
     * @param keys KEYs
     * @return count
     */
    long pfcount(final String... keys);

}
