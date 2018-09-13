package me.lucas.kits.orm.redis.jedis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * Created by zhangxin on 2018/9/11-下午4:19.
 *
 * @author zhangxin
 * @version 1.0
 */
public class RedisHyperLogLogTest {
    private static final String KEY = "pfkey";
    private static final String KEY2 = "pfkey2";
    private Jedis jedis;

    @Before
    public void before() {
        jedis = new Jedis("127.0.0.1", 6379);
    }

    @Test
    public void test() {
        System.out.println(jedis.pfadd(KEY2, "1", "25", "61", "asd"));
        System.out.println(jedis.pfcount(KEY2));
    }

    @After
    public void after() {
        jedis.close();
    }
}
