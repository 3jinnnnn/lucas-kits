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
package me.lucas.kits.orm.redis.jedis.sentinel;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import me.lucas.kits.commons.utils.Assert;
import me.lucas.kits.commons.utils.CollectionUtils;
import me.lucas.kits.orm.redis.jedis.AbstractSentinelRedisClient;
import me.lucas.kits.orm.redis.jedis.exception.RedisClientException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.Tuple;

/**
 * Created by zhangxin on 2018/4/17-上午10:31.
 *
 * @author zhangxin
 * @version 1.0
 */
public class RedisSentinelClientImpl extends AbstractSentinelRedisClient {

    @Override
    public List<Map<String, String>> info() {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return Lists.newArrayList(info0(jedis.info()));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public List<Map<String, String>> info(final String section) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return Lists.newArrayList(info0(jedis.info(section)));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long del(final String... keys) {
        if (keys.length == 0) {
            return 0;
        }

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            final List<Response<Long>> responses = new ArrayList<>();
            for (String key : keys) {
                responses.add(pipeline.del(key));
            }

            pipeline.sync();

            final AtomicLong dels = new AtomicLong(0);
            if (!CollectionUtils.isEmpty(responses)) {
                responses.forEach(res -> dels.addAndGet(res.get()));
            }

            return dels.get();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean exists(final String key) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.exists(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long expire(final String key, final int seconds) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.expire(key, seconds);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long ttl(final String key) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.ttl(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> keys(final String pattern) {
        Assert.hasText(pattern);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return new LinkedHashSet<>(jedis.keys(pattern));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long append(final String key, final String value, final String separator) {
        Assert.hasText(key);
        Assert.hasText(value);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final String newValue;
            if (StringUtils.isNotEmpty(separator) && jedis.exists(key)) {
                newValue = separator + value;
            } else {
                newValue = value;
            }

            return jedis.append(key, newValue);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String get(final String key) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.get(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, String> get(final String... keys) {
        Assert.notEmpty(keys);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipelined = jedis.pipelined();
            final Map<String, Response<String>> values = Maps.newHashMap();
            for (String key : keys) {
                values.put(key, pipelined.get(key));
            }
            pipelined.sync();
            final Map<String, String> valueMap = Maps.newHashMap();
            values.forEach((key, response) -> valueMap.put(key, response.get()));
            return valueMap;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String getset(final String key, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.getSet(key, value);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean set(final String key, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOK(jedis.set(key, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, Boolean> set(final Map<String, Object> map) {
        Assert.notEmpty(map);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipelined = jedis.pipelined();
            final Map<String, Response<String>> responses = Maps.newHashMap();
            map.forEach((key, value) -> responses.put(key, pipelined.set(key, toJSONString(value))));
            pipelined.sync();

            final Map<String, Boolean> values = Maps.newHashMap();
            responses.forEach((key, response) -> values.put(key, isOK(response.get())));
            return values;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean setByNX(final String key, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isSuccess(jedis.setnx(key, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean setByNX(final String key, final String value, final int timeout) {
        Assert.hasText(key);
        Assert.hasText(value);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            final Response<Long> set = pipeline.setnx(key, value);
            final Response<Long> expire = pipeline.expire(key, timeout);
            pipeline.sync();
            return isSuccess(set.get()) && isSuccess(expire.get());
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, Boolean> setByNX(final Map<String, Object> map) {
        Assert.notEmpty(map);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipelined = jedis.pipelined();
            final Map<String, Response<Long>> responses = Maps.newHashMap();
            for (Entry<String, Object> entry : map.entrySet()) {
                responses.put(entry.getKey(), pipelined.setnx(entry.getKey(), toJSONString(entry.getValue())));
            }

            pipelined.sync();
            final Map<String, Boolean> values = Maps.newHashMap();
            responses.forEach((key, response) -> values.put(key, isSuccess(response.get())));
            return values;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean setByEX(final String key, final String value, final int seconds) {
        Assert.hasText(key);
        Assert.hasText(value);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOK(jedis.setex(key, seconds, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long strLen(final String key) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.strlen(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long incr(final String key) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Long val = jedis.incr(key);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long incrBy(final String key, final long value) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Long val = jedis.incrBy(key, value);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public double incrByFloat(final String key, final double value) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Double val = jedis.incrByFloat(key, value);
            if (val == null) {
                return 0;
            }

            return val.doubleValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long decr(final String key) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Long val = jedis.decr(key);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long decrBy(final String key, final long value) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Long val = jedis.decrBy(key, value);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public ScanResult<String> scan(final long cursor, final ScanParams params) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (params == null) {
                return jedis.scan(String.valueOf(cursor), DEFAULT_SCAN_PARAMS);
            } else {
                return jedis.scan(String.valueOf(cursor), params);
            }
        } catch (final Throwable e) {
            if (e instanceof RedisClientException) {
                throw e;
            }

            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long hdel(final String key, final String... fields) {
        Assert.hasText(key);
        Assert.notEmpty(fields);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hdel(key, fields);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean hexists(final String key, final String field) {
        Assert.hasText(key);
        Assert.hasText(field);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hexists(key, field);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String hget(final String key, final String field) {
        Assert.hasText(key);
        Assert.hasText(field);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hget(key, field);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, String> hmget(final String key, final String... fields) {
        Assert.hasText(key);
        Assert.notEmpty(fields);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final List<String> values = jedis.hmget(key, fields);
            final Map<String, String> valuesMap = Maps.newHashMap();
            for (int idx = 0; idx < values.size(); idx++) {
                final String value = values.get(idx);
                if (value != null) {
                    valuesMap.put(fields[idx], value);
                }
            }

            return valuesMap;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, String> hgetAll(final String key) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hgetAll(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> hkeys(final String key) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hkeys(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long hlen(final String key) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hlen(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean hset(final String key, final String field, final String value) {
        Assert.hasText(key);
        Assert.hasText(field);
        Assert.hasText(value);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isSuccess(jedis.hset(key, field, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean hmset(final String key, final Map<String, Object> map) {
        Assert.hasText(key);
        Assert.notEmpty(map);

        Jedis jedis = null;
        try {
            final Map<String, String> newMap = Maps.newHashMap();
            map.forEach((field, value) -> newMap.put(field, toJSONString(value)));
            jedis = getJedis();
            return isOK(jedis.hmset(key, newMap));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean hmset(String key, Map<String, Object> map, Integer ttl) {
        Assert.hasText(key);
        Assert.notEmpty(map);
        Assert.notNull(ttl);

        Jedis jedis = null;
        try {
            final Map<String, String> newMap = Maps.newHashMap();
            map.forEach((field, value) -> newMap.put(field, toJSONString(value)));
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            pipeline.hmset(key, newMap);
            pipeline.expire(key, ttl);
            pipeline.sync();
            return true;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean hsetByNX(final String key, final String field, final String value) {
        Assert.hasText(key);
        Assert.hasText(field);
        Assert.hasText(value);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isSuccess(jedis.hsetnx(key, field, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, Boolean> hsetByNX(final String key, final Map<String, Object> map) {
        Assert.hasText(key);
        Assert.notEmpty(map);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            final Map<String, Response<Long>> responses = Maps.newHashMap();
            for (Entry<String, Object> entry : map.entrySet()) {
                responses.put(entry.getKey(), pipeline.hsetnx(key, entry.getKey(), toJSONString(entry.getValue())));
            }

            pipeline.sync();
            final Map<String, Boolean> values = Maps.newHashMap();
            responses.forEach((field, response) -> values.put(field, isSuccess(response.get())));
            return values;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public List<String> hvals(final String key) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hvals(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public ScanResult<Entry<String, String>> hscan(final String key, final long cursor, final ScanParams params) {
        Assert.hasText(key);
        Assert.notNull(params);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.hscan(key, String.valueOf(cursor), params);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long hincrBy(final String key, final String field, final long value) {
        Assert.hasText(key);
        Assert.hasText(field);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Long val = jedis.hincrBy(key, field, value);
            if (val == null) {
                return 0;
            }

            return val.longValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public double hincrByFloat(final String key, final String field, final double value) {
        Assert.hasText(key);
        Assert.hasText(field);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Double val = jedis.hincrByFloat(key, field, value);
            if (val == null) {
                return 0;
            }

            return val.doubleValue();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String bpop(final String key, final int timeout, final Mark type) {
        Assert.hasText(key);
        Assert.notNull(type);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final List<String> values;
            switch (type) {
            case LPOP:
                values = jedis.blpop(timeout, key);
                break;
            case RPOP:
                values = jedis.brpop(timeout, key);
                break;
            default:
                throw new RedisClientException("Unknown Pop type");
            }

            if (!CollectionUtils.isEmpty(values)) {
                return values.get(1);
            }

            return null;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, String> bpop(final String[] keys, final Mark pop) {
        Assert.notEmpty(keys);
        Assert.notNull(pop);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            final Map<String, String> map = Maps.newHashMap();
            final List<Response<List<String>>> responses = Lists.newArrayList();
            switch (pop) {
            case LPOP:
                for (String key : keys) {
                    responses.add(pipeline.blpop(key));
                }

                break;
            case RPOP:
                for (String key : keys) {
                    responses.add(pipeline.brpop(key));
                }

                break;
            default:
                throw new RedisClientException("Unknown Pop type");
            }

            pipeline.sync();

            responses.forEach(response -> {
                List<String> values = response.get();
                if (values != null && !values.isEmpty()) {
                    map.put(values.get(0), values.get(1));
                }
            });

            return map;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, String> bpop(final String[] keys, final int timeout, final Mark pop) {
        Assert.notEmpty(keys);
        Assert.notNull(pop);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Map<String, String> values = Maps.newHashMap();
            switch (pop) {
            case LPOP:
                for (String key : keys) {
                    final List<String> value = jedis.blpop(timeout, key);
                    if (!CollectionUtils.isEmpty(value)) {
                        values.put(value.get(0), value.get(1));
                    }
                }

                return values;
            case RPOP:
                for (String key : keys) {
                    final List<String> value = jedis.brpop(timeout, key);
                    if (!CollectionUtils.isEmpty(value)) {
                        values.put(value.get(0), value.get(1));
                    }
                }

                return values;
            default:
                throw new RedisClientException("Unknown Pop type");
            }

        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String brpoplpush(final String source, final String destination, final int timeout) {
        Assert.hasText(source);
        Assert.hasText(destination);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.brpoplpush(source, destination, timeout);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String lindex(final String key, final int index) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lindex(key, index);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long linsert(final String key, final String pivot, final String value, final Mark position) {
        Assert.hasText(key);
        Assert.hasText(value);
        Assert.hasText(pivot);
        Assert.notNull(position);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            switch (position) {
            case BEFORE:
                return jedis.linsert(key, LIST_POSITION.BEFORE, pivot, value);
            case AFTER:
                return jedis.linsert(key, LIST_POSITION.AFTER, pivot, value);
            default:
                throw new RedisClientException("Unknown pivot type");
            }
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long llen(final String key) {
        Assert.hasText(key);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.llen(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String pop(final String key, final Mark pop) {
        Assert.hasText(key);
        Assert.notNull(pop);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            switch (pop) {
            case LPOP:
                return jedis.lpop(key);
            case RPOP:
                return jedis.rpop(key);
            default:
                throw new RedisClientException("Unknown pop type");
            }
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public List<String> pop(final String key, int count) {
        Assert.hasText(key);

        final Mark pop;
        if (count >= 0) {
            pop = Mark.LPOP;
        } else {
            pop = Mark.RPOP;
            count = count * -1;
        }

        Jedis jedis = null;
        try {
            final Long len = llen(key);
            if (count > len) {
                count = len.intValue();
            }

            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            final List<Response<String>> responses = Lists.newArrayList();
            switch (pop) {
            case LPOP:
                for (int idx = 0; idx <= count; idx++) {
                    responses.add(pipeline.lpop(key));
                }

                break;
            case RPOP:
                for (int idx = 0; idx <= count; idx++) {
                    responses.add(pipeline.rpop(key));
                }

                break;
            default:
                throw new RedisClientException("Unknown pop type");
            }

            pipeline.sync();

            final List<String> values = Lists.newArrayList();
            responses.forEach(response -> values.add(response.get()));
            return values;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean push(final String key, final String[] values, final Mark push) {
        Assert.hasText(key);
        Assert.notNull(push);
        if (ArrayUtils.isEmpty(values)) {
            return false;
        }

        Jedis jedis = null;
        try {
            jedis = getJedis();
            switch (push) {
            case LPUSH:
                jedis.lpush(key, values);
                break;
            case RPUSH:
                jedis.rpush(key, values);
                break;
            default:
                throw new RedisClientException("未知的写入(PUSH)类型");
            }

            return true;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean push(final String key, final String scanKey, final String value, final Mark push,
            final Mark policy) {
        Assert.hasText(key);
        Assert.hasText(scanKey);
        Assert.hasText(value);
        Assert.notNull(push);
        Assert.notNull(policy);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            switch (push) {
            case LPUSH:
                switch (policy) {
                case KEY:
                    pipeline.lpush(key, scanKey);
                    break;
                case VALUE:
                    pipeline.lpush(key, value);
                    break;
                default:
                    throw new RedisClientException("未知的策略(policy)类型");
                }

                break;
            case RPUSH:
                switch (policy) {
                case KEY:
                    pipeline.rpush(key, scanKey);
                    break;
                case VALUE:
                    pipeline.rpush(key, value);
                    break;
                default:
                    throw new RedisClientException("未知的策略(policy)类型");
                }

                break;
            default:
                throw new RedisClientException("未知的写入(PUSH)类型");
            }

            final Response<String> okResponse = pipeline.set(scanKey, value);
            pipeline.sync();
            return isOK(okResponse.get());
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, Boolean> push(final String key, final Map<String, Object> scanMap, final Mark push,
            final Mark policy) {
        Assert.hasText(key);
        Assert.notEmpty(scanMap);
        Assert.notNull(push);
        Assert.notNull(policy);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            final Map<String, Response<String>> okResponses = Maps.newHashMap();
            for (Entry<String, Object> entry : scanMap.entrySet()) {
                switch (push) {
                case LPUSH:
                    switch (policy) {
                    case KEY:
                        pipeline.lpush(key, entry.getKey());
                        break;
                    case VALUE:
                        pipeline.lpush(key, toJSONString(entry.getValue()));
                        break;
                    default:
                        throw new RedisClientException("未知的策略(policy)类型");
                    }

                    break;
                case RPUSH:
                    switch (policy) {
                    case KEY:
                        pipeline.rpush(key, entry.getKey());
                        break;
                    case VALUE:
                        pipeline.rpush(key, toJSONString(entry.getValue()));
                        break;
                    default:
                        throw new RedisClientException("未知的策略(policy)类型");
                    }

                    break;
                default:
                    throw new RedisClientException("未知的写入(PUSH)类型");
                }

                okResponses.put(entry.getKey(), pipeline.set(entry.getKey(), toJSONString(entry.getValue())));
            }

            pipeline.sync();
            final Map<String, Boolean> values = Maps.newHashMap();
            okResponses.forEach((scanKey, okResponse) -> values.put(scanKey, isOK(okResponse.get())));
            return values;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long pushx(final String key, final String[] values, final Mark push) {
        Assert.hasText(key);
        Assert.notEmpty(values);
        Assert.notNull(push);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            final List<Response<Long>> responses = Lists.newArrayList();
            switch (push) {
            case LPUSH:
                for (String value : values) {
                    responses.add(pipeline.lpushx(key, value));
                }
                break;
            case RPUSH:
                for (String value : values) {
                    responses.add(pipeline.rpushx(key, value));
                }
                break;
            default:
                throw new RedisClientException("未知的写入(PUSH)类型");
            }

            pipeline.sync();
            long pushed = 0;
            for (Response<Long> response : responses) {
                if (pushed == 0) {
                    pushed = response.get();
                } else if (response.get() > 0) {
                    pushed++;
                }

            }

            return pushed;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public List<String> lrange(final String key, final int start, final int end) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lrange(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public List<String> lrangeltrim(final String key, final int count) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            final Response<List<String>> values = pipeline.lrange(key, 0, count);
            pipeline.ltrim(key, count + 1, -1);
            pipeline.sync();
            return values.get();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long lrem(final String key, final int count, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.lrem(key, count, value);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean lset(final String key, final int index, final String value) {
        Assert.hasText(key);
        Assert.hasText(value);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOK(jedis.lset(key, index, value));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean ltrim(final String key, final int start, final int end) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOK(jedis.ltrim(key, start, end));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long sadd(final String key, final String... members) {
        Assert.hasText(key);
        if (members.length == 0) {
            return 0;
        }

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sadd(key, members);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long sreplace(final String key, final String[] oldMembers, final String[] newMembers) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            if (!ArrayUtils.isEmpty(oldMembers)) {
                pipeline.srem(key, oldMembers);
            }

            Response<Long> response = null;
            if (!ArrayUtils.isEmpty(newMembers)) {
                response = pipeline.sadd(key, newMembers);
            }

            pipeline.sync();
            if (response != null) {
                return response.get();
            }

            return 0;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long scard(final String key) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.scard(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Map<String, Long> scard(final String... keys) {
        Assert.notEmpty(keys);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Pipeline pipeline = jedis.pipelined();
            final Map<String, Response<Long>> responses = Maps.newHashMap();
            for (String key : keys) {
                responses.put(key, pipeline.scard(key));
            }

            pipeline.sync();
            final Map<String, Long> values = Maps.newHashMap();
            responses.forEach((key, response) -> values.put(key, response.get()));
            return values;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> sdiff(final String... keys) {
        Assert.notEmpty(keys);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sdiff(keys);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long sdiffstore(final String destination, final String... keys) {
        Assert.hasText(destination);
        Assert.notEmpty(keys);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sdiffstore(destination, keys);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> sinter(final String... keys) {
        Assert.notEmpty(keys);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sinter(keys);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long sinterstore(final String destination, final String... keys) {
        Assert.hasText(destination);
        if (keys.length == 0) {
            return 0;
        }

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sinterstore(destination, keys);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean sismember(final String key, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sismember(key, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> smembers(final String key) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.smembers(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean smove(final String source, final String destination, final String member) {
        Assert.hasText(source);
        Assert.hasText(destination);
        Assert.hasText(member);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isSuccess(jedis.smove(source, destination, member));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String spop(final String key) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.spop(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> spop(final String key, final int count) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.spop(key, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String srandmember(final String key) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.srandmember(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public List<String> srandmember(final String key, final int count) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.srandmember(key, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long srem(final String key, final String... members) {
        Assert.hasText(key);
        if (members.length == 0) {
            return 0;
        }

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.srem(key, members);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> sunion(final String... keys) {
        Assert.notEmpty(keys);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sdiff(keys);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long sunionstore(final String destination, final String... keys) {
        Assert.hasText(destination);
        if (keys.length == 0) {
            return 0;
        }

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sunionstore(destination, keys);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public ScanResult<String> sscan(final String key, final long cursor, final ScanParams params) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sscan(key, String.valueOf(cursor), params);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zadd(final String key, final double score, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zadd(key, score, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zadd(final String key, final Map<Object, Double> values) {
        Assert.hasText(key);
        Assert.notEmpty(values);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            long changed = 0;
            for (Entry<Object, Double> value : values.entrySet()) {
                changed += zadd(key, value.getValue(), value.getKey());
            }

            return changed;
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zcard(final String key) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zcard(key);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zcount(final String key, final double min, final double max) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zcount(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zcount(final String key, final String min, final String max) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zcount(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zlexcount(final String key, final String min, final String max) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zlexcount(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public double zincrby(final String key, final double increment, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zincrby(key, increment, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> zrange(final String key, final long start, final long end) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrange(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max, final int offset,
            final int count) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrangeByLex(key, min, max, offset, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public <T> Map<T, Double> zrangeWithScores(final String key, final long start, final long end,
            final TypeReference<T> type) {
        Assert.hasText(key);
        Assert.notNull(type);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Set<Tuple> values = jedis.zrangeWithScores(key, start, end);
            if (!CollectionUtils.isEmpty(values)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple value : values) {
                    newValues.put(parseObject(value.getElement(), type), value.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrangeByScore(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max, final int offset,
            final int count) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrangeByScore(key, min, max, offset, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public <T> Map<T, Double> zrangeByScoreWithScores(final String key, final String min, final String max,
            final TypeReference<T> type) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Set<Tuple> values = jedis.zrangeByScoreWithScores(key, min, max);
            if (!CollectionUtils.isEmpty(values)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple value : values) {
                    newValues.put(parseObject(value.getElement(), type), value.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public <T> Map<T, Double> zrangeByScoreWithScores(final String key, final String min, final String max,
            final int offset, final int count, final TypeReference<T> type) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Set<Tuple> values = jedis.zrangeByScoreWithScores(key, min, max, offset, count);
            if (!CollectionUtils.isEmpty(values)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple value : values) {
                    newValues.put(parseObject(value.getElement(), type), value.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zrank(final String key, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrank(key, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zrem(final String key, final String... members) {
        Assert.hasText(key);
        Assert.notEmpty(members);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrem(key, members);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zremrangeByLex(final String key, final String min, final String max) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zremrangeByLex(key, min, max);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zremrangeByRank(final String key, final long start, final long end) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zremrangeByRank(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zremrangeByScore(final String key, final String start, final String end) {
        Assert.hasText(key);
        Assert.hasText(start);
        Assert.hasText(end);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zremrangeByScore(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> zrevrange(final String key, final long start, final long end) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrevrange(key, start, end);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrevrangeByLex(key, max, min);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min, final int offset,
            final int count) {
        Assert.hasText(key);
        Assert.hasText(min);
        Assert.hasText(max);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrevrangeByLex(key, max, min, offset, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public <T> Map<T, Double> zrevrangeWithScores(final String key, final long start, final long end,
            final TypeReference<T> type) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Set<Tuple> values = jedis.zrevrangeWithScores(key, start, end);
            if (!CollectionUtils.isEmpty(values)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple value : values) {
                    newValues.put(parseObject(value.getElement(), type), value.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrevrangeByScore(key, max, min);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min, final int offset,
            final int count) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrevrangeByScore(key, max, min, offset, count);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public <T> Map<T, Double> zrevrangeByScoreWithScores(final String key, final double max, final double min,
            final TypeReference<T> type) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Set<Tuple> values = jedis.zrevrangeByScoreWithScores(key, max, min);
            if (!CollectionUtils.isEmpty(values)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple value : values) {
                    newValues.put(parseObject(value.getElement(), type), value.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public <T> Map<T, Double> zrevrangeByScoreWithScores(final String key, final double max, final double min,
            final int offset, final int count, final TypeReference<T> type) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            final Set<Tuple> values = jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
            if (!CollectionUtils.isEmpty(values)) {
                final Map<T, Double> newValues = Maps.newHashMap();
                for (Tuple value : values) {
                    newValues.put(parseObject(value.getElement(), type), value.getScore());
                }

                return newValues;
            }

            return Collections.emptyMap();
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long zrevrank(final String key, final String member) {
        Assert.hasText(key);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zrevrank(key, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public double zscore(final String key, final String member) {
        Assert.hasText(key);
        Assert.hasText(member);
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.zscore(key, member);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public ScanResult<Entry<String, Double>> zscan(final String key, final long cursor, final ScanParams params) {
        Assert.hasText(key);
        Assert.notNull(params);

        Jedis jedis = null;
        try {
            jedis = getJedis();
            final ScanResult<Tuple> res = jedis.zscan(key, String.valueOf(cursor), params);
            final List<Tuple> tuples = res.getResult();
            if (CollectionUtils.isEmpty(tuples)) {
                return new ScanResult<>(res.getStringCursor(), Collections.emptyList());
            }

            final List<Entry<String, Double>> newTuples = Lists.newArrayList();
            tuples.forEach(tuple -> newTuples.add(new AbstractMap.SimpleEntry<>(tuple.getElement(), tuple.getScore())));
            return new ScanResult<>(res.getStringCursor(), newTuples);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        Jedis jedis = null;
        try {
            jedis = getJedis();

            return jedis.eval(script, keyCount, params);

        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.eval(script, keys, args);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Object eval(String script) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.eval(script);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Object evalsha(String script) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.evalsha(script);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.evalsha(sha1, keys, args);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Object evalsha(String sha1, int keyCount, String... params) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.evalsha(sha1, keyCount, params);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Boolean scriptExists(String sha1) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.scriptExists(sha1);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public List<Boolean> scriptExists(String... sha1) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.scriptExists(sha1);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public String scriptLoad(String script) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.scriptLoad(script);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public Long pfadd(String key, String... elements) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.pfadd(key, elements);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public boolean pfmerge(String destkey, String... sourcekeys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOK(jedis.pfmerge(destkey, sourcekeys));
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long pfcount(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.pfcount(key);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }

    @Override
    public long pfcount(String... keys) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.pfcount(keys);
        } catch (Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        } finally {
            close(jedis);
        }
    }
}
