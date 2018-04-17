package me.lucas.kits.orm.redis.jedis;

import com.alibaba.fastjson.JSON;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.commons.loader.PropertiesLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Created by zhangxin on 2018/4/13-下午1:50.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
//此注解用来加载配置ApplicationContext
@ContextConfiguration(classes = { RedisClientPool.class, TestConfig.class })
@WebAppConfiguration
public class RedisClientTest {

    @Autowired
    @Qualifier("shiro")
    private ShardedJedisPool shiro;

    @Autowired
    @Qualifier("shiro")
    private RedisClient redis;

    @Test
    public void test() {
        Properties properties = PropertiesLoader.load("redis.properties");
        log.info(JSON.toJSONString(properties));
        String info = shiro.getResource().get("123");
        log.info(JSON.toJSONString(info));
    }
}