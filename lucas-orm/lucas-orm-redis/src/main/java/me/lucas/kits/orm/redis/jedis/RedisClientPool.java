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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.commons.loader.LoaderException;
import me.lucas.kits.commons.loader.PropertiesLoader;
import me.lucas.kits.commons.utils.Assert;
import me.lucas.kits.commons.utils.ReflectUtils;
import me.lucas.kits.orm.redis.jedis.cluster.RedisClusterClientImpl;
import me.lucas.kits.orm.redis.jedis.exception.RedisClientException;
import me.lucas.kits.orm.redis.jedis.sharded.RedisClientImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

/**
 * RedisClient连接池管理类.
 *
 * @author yanghe
 * @since 1.0
 */
@Slf4j
@Component
public class RedisClientPool implements ApplicationContextAware {
    public static final String MAIN_REDIS = "/redis.properties";
    public static final String POOL_PRIFIX = "pool:";
    public static final String REDIS_PRIFIX = "redis:";

    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int DEFAULT_MAX_REDIRECTIONS = 5;
    private static final int DEFAULT_MAX_TOTAL = 100;
    private static final int DEFAULT_MAX_IDLE = 30;
    private static final int DEFAULT_MIN_IDLE = 10;
    private static final Boolean DEFAULT_TEST_ON_BORROW = Boolean.FALSE;

    private static ApplicationContext APPLICATION_CONTEXT;
    private static BeanDefinitionRegistry BEAN_FACTORY;

    /** REDIS连接池，可以对应的是操作类型 */
    private Map<String, ShardedJedisPool> jedisPool = Maps.newHashMap();

    private Map<String, JedisCluster> jedisClusterPool = Maps.newHashMap();

    private Map<String, RedisConfig> redisConfigs = Maps.newLinkedHashMap();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        APPLICATION_CONTEXT = applicationContext;
    }

    @PostConstruct
    public void init() {
        Properties properties = PropertiesLoader.load(MAIN_REDIS);
        try {
            initRedisConfig(properties);
            BEAN_FACTORY = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) APPLICATION_CONTEXT)
                    .getBeanFactory();
            createJedis();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public RedisClientPool initRedisConfig(final List<Properties> redis) throws LoaderException, IOException {
        if (redis == null || redis.isEmpty()) {
            return this;
        }

        return initRedisConfig(redis.toArray(new Properties[redis.size()]));
    }

    public RedisClientPool initRedisConfig(final Properties... redis) throws LoaderException, IOException {
        List<Properties> redises = new ArrayList<>();
        if (redis == null || redis.length == 0) {
            redises.add(PropertiesLoader.load(MAIN_REDIS));
        } else {
            redises.addAll(Arrays.asList(redis));
        }

        /** 修正了因有多个idx导致的config加载的BUG. */
        for (Properties rds : redises) {
            final String root = rds.getProperty(RedisConfig.ROOT);
            if (StringUtils.isNotEmpty(root)) {
                final Map<String, RedisConfig> confs = Maps.newHashMap();
                final String[] idxs = root.split(",");
                for (String idx : idxs) {
                    final RedisConfig conf = RedisConfig.newInstance();
                    for (String name : conf.attributeNames()) {
                        if (RedisConfig.REDIS_TYPE.equals(name)) {
                            Assert.hasLength(rds.getProperty(RedisConfig.REDIS + idx + '.' + name));
                        } else if (RedisConfig.EXTEND_PROPERTIES.equals(name)) {
                            continue;
                        }

                        conf.setAttributeValue(name, rds.getProperty(RedisConfig.REDIS + idx + '.' + name));
                    }

                    confs.put(conf.getRedisType(), conf);
                }

                redisConfigs.putAll(confs);
            }
        }

        return this;
    }

    /**
     * 初始化连接池.
     */
    public void createJedis() {
        redisConfigs.values().stream().filter(conf -> conf.getCluster() == null || !conf.getCluster())
                .forEach(conf -> jedisPool.put(conf.getRedisType(), createJedisPool(conf)));

        redisConfigs.values().stream().filter(conf -> conf.getCluster() != null && conf.getCluster())
                .forEach(conf -> jedisClusterPool.put(conf.getRedisType(), createJedisClusterPool(conf)));
    }

    public ShardedJedisPool appendJedis(final RedisConfig conf) {
        Assert.notNull(conf);
        Assert.hasLength(conf.getRedisType());

        if (conf.getCluster() == null || !conf.getCluster()) {
            if (!jedisPool.containsKey(conf.getRedisType())) {
                redisConfigs.put(conf.getRedisType(), conf);
                final ShardedJedisPool pool;
                jedisPool.put(conf.getRedisType(), pool = createJedisPool(conf));
                return pool;
            }

            return jedisPool.get(conf.getRedisType());
        }

        throw new RedisClientException("Can't append ShardedJedis, this is a redis cluster config");
    }

    public JedisCluster appendJedisCluster(RedisConfig conf) {
        Assert.notNull(conf);
        Assert.hasLength(conf.getRedisType());

        if (conf.getCluster() != null && conf.getCluster()) {
            if (!jedisClusterPool.containsKey(conf.getRedisType())) {
                redisConfigs.put(conf.getRedisType(), conf);
                final JedisCluster cluster;
                jedisClusterPool.put(conf.getRedisType(), cluster = createJedisClusterPool(conf));
                return cluster;
            }
        }

        throw new RedisClientException("Can't append JedisCluster, this is a redis sharded config");
    }

    /**
     * 根据连接池名，取得连接
     *
     * @param poolName poolName
     * @return ShardedJedis
     */
    public ShardedJedis getJedis(final String poolName) {
        Assert.hasText(poolName);
        ShardedJedis shardedJedis = null;
        try {
            final ShardedJedisPool pool = jedisPool.get(poolName);
            if (pool != null) {
                shardedJedis = pool.getResource();
            }

            Assert.notNull(shardedJedis, "Not found ShardedJedis.");
            return shardedJedis;
        } catch (final Throwable e) {
            close(shardedJedis);
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    public Map<String, ShardedJedisPool> getJedisPool() {
        return this.jedisPool;
    }

    public Map<String, JedisCluster> getJedisClusterPool() {
        return this.jedisClusterPool;
    }

    public JedisCluster getJedisCluster(final String poolName) {
        Assert.hasText(poolName);
        final JedisCluster cluster = jedisClusterPool.get(poolName);
        Assert.notNull(cluster, "Not found JedisCluster.");
        return cluster;
    }

    /**
     * 创建连接池.
     *
     * @param conf RedisConfig
     * @return ShardedJedisPool
     */
    private ShardedJedisPool createJedisPool(final RedisConfig conf) {
        Assert.notNull(conf);
        try {
            final String[] hostAndports = conf.getHostNames().split(";");
            final List<String> redisHosts = Lists.newArrayList();
            final List<Integer> redisPorts = Lists.newArrayList();
            for (int i = 0; i < hostAndports.length; i++) {
                final String[] hostPort = hostAndports[i].split(":");
                redisHosts.add(hostPort[0]);
                redisPorts.add(Integer.valueOf(hostPort[1]));
            }

            final List<JedisShardInfo> shards = Lists.newArrayList();
            for (int i = 0; i < redisHosts.size(); i++) {
                final String host = (String) redisHosts.get(i);
                final Integer port = (Integer) redisPorts.get(i);
                Integer timeout = conf.getTimeOut();
                if (timeout == null || timeout < 0) {
                    timeout = DEFAULT_TIMEOUT;
                }

                JedisShardInfo si = new JedisShardInfo(host, port.intValue(), timeout);
                shards.add(si);
            }

            // 注册ShardedJedisPool
            BeanDefinitionBuilder poolBeanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(ShardedJedisPool.class);
            poolBeanDefinitionBuilder.addConstructorArgValue(getJedisPoolConfig(conf));
            poolBeanDefinitionBuilder.addConstructorArgValue(shards);
            poolBeanDefinitionBuilder.addConstructorArgValue(Hashing.MURMUR_HASH);
            poolBeanDefinitionBuilder.addConstructorArgValue(Sharded.DEFAULT_KEY_TAG_PATTERN);
            BeanDefinition poolBeanDefinition = poolBeanDefinitionBuilder.getBeanDefinition();
            BEAN_FACTORY.registerBeanDefinition(POOL_PRIFIX + conf.getRedisType(), poolBeanDefinition);

            // 注册RedisClientImpl
            BeanDefinitionBuilder clientBeanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(RedisClientImpl.class);
            clientBeanDefinitionBuilder.addPropertyValue("config", conf);
            ShardedJedisPool jedisPool = APPLICATION_CONTEXT
                    .getBean(POOL_PRIFIX + conf.getRedisType(), ShardedJedisPool.class);
            clientBeanDefinitionBuilder.addPropertyReference("pool", POOL_PRIFIX + conf.getRedisType());
            BeanDefinition clientBeanDefinition = clientBeanDefinitionBuilder.getBeanDefinition();
            BEAN_FACTORY.registerBeanDefinition(REDIS_PRIFIX + conf.getRedisType(), clientBeanDefinition);
            RedisClientImpl redisClient = APPLICATION_CONTEXT
                    .getBean(REDIS_PRIFIX + conf.getRedisType(), RedisClientImpl.class);
            return new ShardedJedisPool(getJedisPoolConfig(conf), shards, Hashing.MURMUR_HASH,
                    Sharded.DEFAULT_KEY_TAG_PATTERN);
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    private JedisCluster createJedisClusterPool(final RedisConfig config) {
        Assert.notNull(config);
        try {
            final String[] hostAndports = config.getHostNames().split(";");
            final List<String> redisHosts = Lists.newArrayList();
            final List<Integer> redisPorts = Lists.newArrayList();
            for (int i = 0; i < hostAndports.length; i++) {
                final String[] hostPort = hostAndports[i].split(":");
                redisHosts.add(hostPort[0]);
                redisPorts.add(Integer.valueOf(hostPort[1]));
            }

            final Set<HostAndPort> nodes = Sets.newLinkedHashSet();
            for (int i = 0; i < redisHosts.size(); i++) {
                final String host = (String) redisHosts.get(i);
                final int port = (Integer) redisPorts.get(i);
                nodes.add(new HostAndPort(host, port));
            }

            Integer timeout = config.getTimeOut();
            if (timeout == null || timeout < 0) {
                timeout = DEFAULT_TIMEOUT;
            }

            Integer maxRedirections = config.getMaxRedirections();
            if (maxRedirections == null || maxRedirections < 0) {
                maxRedirections = DEFAULT_MAX_REDIRECTIONS;
            }

            return new JedisCluster(nodes, timeout, maxRedirections, getJedisPoolConfig(config));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    /**
     * 设置redis连接池的属性
     *
     * @param conf RedisConfig
     * @return JedisPoolConfig
     */
    private JedisPoolConfig getJedisPoolConfig(final RedisConfig conf) {
        Assert.notNull(conf);

        Integer maxTotal = conf.getMaxTotal();
        if (maxTotal == null) {
            maxTotal = DEFAULT_MAX_TOTAL;
        }

        Integer maxIdle = conf.getMaxIdle();
        if (maxIdle == null) {
            maxIdle = DEFAULT_MAX_IDLE;
        }

        Integer minIdle = conf.getMinIdle();
        if (minIdle == null) {
            minIdle = DEFAULT_MIN_IDLE;
        }

        Boolean testOnBorrow = conf.getTestOnBorrow();
        if (testOnBorrow == null) {
            testOnBorrow = DEFAULT_TEST_ON_BORROW;
        }

        final JedisPoolConfig poolConf = new JedisPoolConfig();
        poolConf.setMaxTotal(maxTotal);
        poolConf.setMaxIdle(maxIdle);
        poolConf.setTestOnBorrow(testOnBorrow);
        poolConf.setMinIdle(minIdle);
        return poolConf;
    }

    public Map<String, RedisConfig> getRedisConfigs() {
        return redisConfigs;
    }

    public RedisConfig getRedisConfig(final String redisType) {
        Assert.hasLength(redisType);
        final RedisConfig conf;
        if ((conf = redisConfigs.get(redisType)) == null) {
            throw new RedisClientException("无效的RedisType");
        }

        return conf;
    }

    public void close(final ShardedJedis shardedJedis) {
        if (shardedJedis == null) {
            return;
        }
        shardedJedis.close();
    }

}
