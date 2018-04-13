package me.lucas.kits.orm.redis.jedis;

import java.util.Properties;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.commons.loader.PropertiesLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by zhangxin on 2018/4/13-下午1:50.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
//此注解用来加载配置ApplicationContext
@ContextConfiguration(classes = { RedisClientBeanDefinitionRegistryPostProcessor.class, TestConfig.class })
@WebAppConfiguration
public class RedisClientTest {
    @Value("#{propertiesReader['reids.root']}")
    private String redisRoots;

    @Test
    public void test() {
        Properties properties = PropertiesLoader.load("redis.properties");

        System.out.println(redisRoots);
        log.info(redisRoots);
    }
}
