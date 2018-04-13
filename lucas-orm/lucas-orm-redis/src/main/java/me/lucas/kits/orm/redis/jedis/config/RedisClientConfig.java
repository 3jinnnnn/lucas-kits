package me.lucas.kits.orm.redis.jedis.config;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletConfig;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.commons.loader.PropertiesLoader;
import me.lucas.kits.commons.utils.CollectionUtils;
import me.lucas.kits.orm.redis.jedis.RedisClientPool;
import me.lucas.kits.orm.redis.jedis.sharded.RedisClientImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhangxin on 2018/3/28-下午3:10.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@Configuration
public class RedisClientConfig implements ApplicationContextAware {
    private static ApplicationContext APPLICATION_CONTEXT;
    private static final String DEFAULT_REDIS_PARAMETER_NAME = "redis";
    private static final String DEFAULT_REDIS_PATH = "/redis.properties";
    private final List<Properties> properties = Lists.newArrayList();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        APPLICATION_CONTEXT = applicationContext;
    }

//    @PostConstruct
    public void initMethod() {
        try {
            final long time = System.currentTimeMillis();
            final RedisClientPool pool = RedisClientPool.getInstance();
            pool.initRedisConfig();
            pool.createJedis();
            pool.bindGlobal();
            log.info("加载Redis配置, 耗时: " + (System.currentTimeMillis() - time) + "ms");
        } catch (final Throwable e) {
            if (!(e instanceof ClassNotFoundException)) {
            }
        }
    }

    public void config(final ServletConfig config) throws Throwable {
        final String redis = config.getInitParameter(DEFAULT_REDIS_PARAMETER_NAME);
        if (StringUtils.isNotBlank(redis)) {
            final String[] paths = redis.split(";");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        final String contextRedis = System.getProperty("context.redis");
        if (StringUtils.isNotBlank(contextRedis)) {
            final String[] paths = contextRedis.split(";");
            for (final String path : paths) {
                properties.add(PropertiesLoader.load(path));
            }
        }

        if (CollectionUtils.isEmpty(properties)) {
            try {
                properties.add(PropertiesLoader.load(DEFAULT_REDIS_PATH));
            } catch (final Throwable e) {
                // ignore
            }
        }
    }

    private void regist() {
        DefaultListableBeanFactory parentBeanFactory = (DefaultListableBeanFactory) APPLICATION_CONTEXT
                .getParentBeanFactory();
        BeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(RedisClientImpl.class);
        parentBeanFactory.registerBeanDefinition("", beanDefinition);
        //        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        //        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry);
        //        reader.register(JedisClient.class);

    }

}
