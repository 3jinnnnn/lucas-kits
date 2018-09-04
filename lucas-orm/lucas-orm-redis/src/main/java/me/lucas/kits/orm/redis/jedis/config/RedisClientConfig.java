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
package me.lucas.kits.orm.redis.jedis.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.commons.loader.LoaderException;
import me.lucas.kits.commons.loader.PropertiesLoader;
import me.lucas.kits.commons.utils.Assert;
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
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

/**
 * RedisClient 配置类.
 * 加载配置文件,向Spring容器中注册
 * 1. ShardedJedisPool  命名规则: "pool:"+ 配置文件的redisType
 * 2. RedisClientImpl   命名规则: "pool:"+ 配置文件的redisType
 *
 * @author zhangxin
 * @since 1.0
 */
@Slf4j
@Component
public class RedisClientConfig implements ApplicationContextAware {
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

    public void initRedisConfig(final Properties... redis) throws LoaderException, IOException {
        List<Properties> redises = new ArrayList<>();
        if (redis == null || redis.length == 0) {
            redises.add(PropertiesLoader.load(MAIN_REDIS));
        } else {
            redises.addAll(Arrays.asList(redis));
        }
        // 修正了因有多个idx导致的config加载的BUG.
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
    }

    /**
     * 初始化连接池.
     */
    public void createJedis() {
        redisConfigs.values().stream()
                .filter(conf -> (conf.getCluster() == null || !conf.getCluster()) && (conf.getSentinel() == null
                        || !conf.getSentinel())).forEach(this::createJedisPool);
        redisConfigs.values().stream().filter(conf -> conf.getCluster() != null && conf.getCluster())
                .forEach(this::createJedisClusterPool);
        redisConfigs.values().stream().filter(conf -> conf.getSentinel() != null && conf.getSentinel())
                .forEach(this::createJedisSentinelPool);
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
            clientBeanDefinitionBuilder.addPropertyReference("pool", POOL_PRIFIX + conf.getRedisType());
            BeanDefinition clientBeanDefinition = clientBeanDefinitionBuilder.getBeanDefinition();
            BEAN_FACTORY.registerBeanDefinition(REDIS_PRIFIX + conf.getRedisType(), clientBeanDefinition);
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
            String master = null;
            final Set<String> sentinels = Sets.newHashSet();
            for (int i = 0; i < hostAndports.length; i++) {
                if (i == 0) {
                    master = hostAndports[i];
                } else {
                    sentinels.add(hostAndports[i]);
                }
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
            // 注册ShardedJedisPool
            BeanDefinitionBuilder poolBeanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(JedisSentinelPool.class);
            poolBeanDefinitionBuilder.addConstructorArgValue(master);
            poolBeanDefinitionBuilder.addConstructorArgValue(sentinels);
            poolBeanDefinitionBuilder.addConstructorArgValue(getJedisPoolConfig(config));
            poolBeanDefinitionBuilder.addConstructorArgValue(timeout);
            BeanDefinition poolBeanDefinition = poolBeanDefinitionBuilder.getBeanDefinition();
            BEAN_FACTORY.registerBeanDefinition(POOL_PRIFIX + config.getRedisType(), poolBeanDefinition);
            // 注册RedisClientImpl
            BeanDefinitionBuilder clientBeanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(RedisClientImpl.class);
            clientBeanDefinitionBuilder.addPropertyValue("config", config);
            clientBeanDefinitionBuilder.addPropertyReference("pool", POOL_PRIFIX + config.getRedisType());
            BeanDefinition clientBeanDefinition = clientBeanDefinitionBuilder.getBeanDefinition();
            BEAN_FACTORY.registerBeanDefinition(REDIS_PRIFIX + config.getRedisType(), clientBeanDefinition);
            return new JedisCluster(nodes, timeout, maxRedirections, getJedisPoolConfig(config));
        } catch (final Throwable e) {
            throw new RedisClientException(e.getMessage(), e);
        }
    }

    private JedisSentinelPool createJedisSentinelPool(final RedisConfig config) {
        Assert.notNull(config);
        try {
            final String[] hostAndports = config.getHostNames().split(";");
            final List<String> redisHosts = Lists.newArrayList();
            final List<Integer> redisPorts = Lists.newArrayList();
            String master = null;
            final Set<String> sentinels = Sets.newHashSet();
            for (int i = 0; i < hostAndports.length; i++) {
                if (i == 0) {
                    master = hostAndports[i];
                } else {
                    sentinels.add(hostAndports[i]);
                }
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
            // 注册ShardedJedisPool
            BeanDefinitionBuilder poolBeanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(JedisSentinelPool.class);
            poolBeanDefinitionBuilder.addConstructorArgValue(master);
            poolBeanDefinitionBuilder.addConstructorArgValue(sentinels);
            poolBeanDefinitionBuilder.addConstructorArgValue(getJedisPoolConfig(config));
            poolBeanDefinitionBuilder.addConstructorArgValue(timeout);
            BeanDefinition poolBeanDefinition = poolBeanDefinitionBuilder.getBeanDefinition();
            BEAN_FACTORY.registerBeanDefinition(POOL_PRIFIX + config.getRedisType(), poolBeanDefinition);
            // 注册RedisClientImpl
            BeanDefinitionBuilder clientBeanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(RedisClientImpl.class);
            clientBeanDefinitionBuilder.addPropertyValue("config", config);
            clientBeanDefinitionBuilder.addPropertyReference("pool", POOL_PRIFIX + config.getRedisType());
            BeanDefinition clientBeanDefinition = clientBeanDefinitionBuilder.getBeanDefinition();
            BEAN_FACTORY.registerBeanDefinition(REDIS_PRIFIX + config.getRedisType(), clientBeanDefinition);
            return new JedisSentinelPool(config.getRedisType(), Sets.newHashSet(hostAndports), getJedisPoolConfig(config), timeout);
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

}
