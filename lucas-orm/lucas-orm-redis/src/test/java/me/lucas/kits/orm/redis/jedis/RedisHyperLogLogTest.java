package me.lucas.kits.orm.redis.jedis;

import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.orm.redis.jedis.config.RedisClientConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by zhangxin on 2018/9/11-下午4:19.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
//此注解用来加载配置ApplicationContext
@ContextConfiguration(classes = { RedisClientConfig.class, SpringConfig.class })
@WebAppConfiguration
@FixMethodOrder(MethodSorters.JVM)
public class RedisHyperLogLogTest {
    private static final String KEY = "pfkey";
    private static final String KEY2 = "pfkey2";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier(RedisClientConfig.REDIS_PRIFIX + "main")
    private RedisClient redis;


    @Before
    public void before() {
        redis.pfadd(KEY, "1","2","3");
        redis.pfadd(KEY2, "2","3","4");
    }

    @Test
    public void base() {
        Assert.assertEquals(redis.pfcount(KEY), 3);
        Assert.assertEquals(redis.pfcount(KEY2), 3);
    }

    @Test
    public void add() {
        redis.pfadd(KEY, "3");
        Assert.assertEquals(redis.pfcount(KEY), 3);
    }

    @Test
    public void merge() {
        redis.pfmerge(KEY, KEY2);
        Assert.assertEquals(redis.pfcount(KEY), 4);
    }

    @After
    public void after() {
        redis.del(KEY, KEY2);
    }
}
