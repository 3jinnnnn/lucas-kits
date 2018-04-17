package me.lucas.kits.orm.redis.jedis;

import com.alibaba.fastjson.JSON;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.commons.loader.PropertiesLoader;
import me.lucas.kits.orm.redis.jedis.config.RedisClientConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@ContextConfiguration(classes = { RedisClientConfig.class, TestConfig.class })
@WebAppConfiguration
public class RedisClientTest {

    @Autowired
    @Qualifier(RedisClientConfig.REDIS_PRIFIX + "shiro")
    private RedisClient redis;

    @Test
    public void test() {
        Properties properties = PropertiesLoader.load("redis.properties");
        log.info(JSON.toJSONString(properties));
        log.info(redis.get("123"));
        //        String info = shiro.getResource().get("123");
        //        log.info(JSON.toJSONString(info));
    }
}
