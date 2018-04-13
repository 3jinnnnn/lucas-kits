package me.lucas.kits.orm.redis.jedis;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.commons.loader.LoaderException;
import me.lucas.kits.commons.loader.PropertiesLoader;
import me.lucas.kits.commons.utils.Assert;
import me.lucas.kits.commons.utils.StringUtils;
import me.lucas.kits.orm.redis.jedis.sharded.RedisClientImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.util.StringValueResolver;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Created by zhangxin on 2018/4/13-上午11:37.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@Configuration
public class RedisClientBeanDefinitionRegistryPostProcessor
        implements EmbeddedValueResolverAware, BeanDefinitionRegistryPostProcessor {
    private static final String PROPERTIES_KEY_PRIFIX = "${";
    private static final String PROPERTIES_KEY_SUFFIX = "}";

    private static StringValueResolver STRING_VALUE_RESOLVER;

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
    private Map<String, ShardedJedisPool> jedisPool = Maps.newHashMap();
    private Map<String, JedisCluster> jedisClusterPool = Maps.newHashMap();
    private Map<String, RedisConfig> redisConfigs = Maps.newLinkedHashMap();

    private String[] redisRoots;

    @Autowired
    public void setRedisRoots(
            @Value(PROPERTIES_KEY_PRIFIX + RedisConfig.ROOT + PROPERTIES_KEY_SUFFIX) String redisNames) {
        if (StringUtils.isNotBlank(redisNames)) {
            this.redisRoots = redisNames.split(",");
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //        Map<String, ShardedJedisPool> jedisPool = pool.jedisPool;
        //        registerBean(registry, "shanhyA", RedisClientImpl.class);
        Properties properties = PropertiesLoader.load("redis.properties");
        String property = properties.getProperty("redis.root");
        if (StringUtils.isNotBlank(property)) {

        }
        log.info(property);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //        BeanDefinition bd = beanFactory.getBeanDefinition("RedisClientImpl");
        //        MutablePropertyValues mpv = bd.getPropertyValues();
        String value = STRING_VALUE_RESOLVER
                .resolveStringValue(PROPERTIES_KEY_PRIFIX + RedisConfig.ROOT + PROPERTIES_KEY_SUFFIX);
        log.info(value);
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        STRING_VALUE_RESOLVER = resolver;
    }

    private void registerBean(BeanDefinitionRegistry registry, String name, Class<?> beanClass) {
        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);

        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        // 可以自动生成name
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, registry));

        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);

        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }

    @PostConstruct
    public void initMethod() {
        try {
            final long time = System.currentTimeMillis();
            initRedisConfig();
            log.info("加载Redis配置, 耗时: " + (System.currentTimeMillis() - time) + "ms");
        } catch (final Throwable e) {
            if (!(e instanceof ClassNotFoundException)) {
            }
            log.error(e.getMessage(), e);
        }
    }

    public void initRedisConfig() throws LoaderException, IOException {
        if (redisRoots != null && redisRoots.length != 0) {
            final Map<String, RedisConfig> confs = Maps.newHashMap();
            for (String redisRoot : redisRoots) {
                final RedisConfig conf = RedisConfig.newInstance();
                for (String filedName : conf.attributeNames()) {
                    if (RedisConfig.REDIS_TYPE.equals(filedName)) {
                        String value = STRING_VALUE_RESOLVER.resolveStringValue(
                                new StringBuilder(PROPERTIES_KEY_PRIFIX).append(RedisConfig.REDIS).append(redisRoot)
                                        .append(RedisConfig.SEPARATOR).append(RedisConfig.REDIS_TYPE)
                                        .append(PROPERTIES_KEY_SUFFIX).toString());
                        Assert.hasLength(value);
                    } else if (RedisConfig.EXTEND_PROPERTIES.equals(filedName)) {
                        continue;
                    }
                    try {

                        String value = STRING_VALUE_RESOLVER.resolveStringValue(
                                new StringBuilder(PROPERTIES_KEY_PRIFIX).append(RedisConfig.REDIS).append(redisRoot)
                                        .append(RedisConfig.SEPARATOR).append(filedName).append(PROPERTIES_KEY_SUFFIX)
                                        .toString());
                        conf.setAttributeValue(filedName, value);
                    } catch (Exception e) {
                    }
                }
                confs.put(conf.getRedisType(), conf);
            }
            redisConfigs.putAll(confs);
        }
    }

}
