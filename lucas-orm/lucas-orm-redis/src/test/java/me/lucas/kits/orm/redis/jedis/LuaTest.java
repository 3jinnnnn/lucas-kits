package me.lucas.kits.orm.redis.jedis;

import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.commons.utils.UUIDUtils;
import me.lucas.kits.orm.redis.jedis.commands.ScriptingCommands.RefreshType;
import me.lucas.kits.orm.redis.jedis.commands.ScriptingCommands.ResultType;
import me.lucas.kits.orm.redis.jedis.config.RedisClientConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by zhangxin on 2018/9/4-下午9:03.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
//此注解用来加载配置ApplicationContext
@ContextConfiguration(classes = { RedisClientConfig.class, TestConfig.class })
@WebAppConfiguration
public class LuaTest {
    private static final String VALUE1 = "VALUE1";
    private static final String VALUE2 = "VALUE2";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier(RedisClientConfig.REDIS_PRIFIX + "main")
    private RedisClient redis;

    private String key;

    @Before
    public void before() {
        key = UUIDUtils.random();
    }

    /**
     * 查询.
     */
    @Test
    public void select() {
        ResultType resultType1 = redis.refreshCache(this.key, VALUE1, 60);
        Assert.assertEquals(ResultType.SUCCESS_SAVE, resultType1);
        Assert.assertEquals(VALUE1, redis.get(this.key));
        ResultType resultType2 = redis.refreshCache(this.key, VALUE2, 60);
        Assert.assertEquals(ResultType.SUCCESS_REFRESH, resultType2);
        Assert.assertEquals(VALUE2, redis.get(this.key));
    }

    /**
     * 查询->更新.
     */
    @Test
    public void selectUpdate() {
        ResultType resultType1 = redis.refreshCache(this.key, VALUE1, 60);
        Assert.assertEquals(ResultType.SUCCESS_SAVE, resultType1);
        Assert.assertEquals(VALUE1, redis.get(this.key));
        ResultType resultType2 = redis.refreshCache(this.key, VALUE2, 60, RefreshType.UPDATE);
        Assert.assertEquals(ResultType.SUCCESS_REFRESH, resultType2);
        Assert.assertEquals(VALUE2, redis.get(this.key));
    }

    /**
     * 更新->查询.
     */
    @Test
    public void updateSelect() {
        ResultType resultType1 = redis.refreshCache(this.key, VALUE1, 60, RefreshType.UPDATE);
        Assert.assertEquals(ResultType.SUCCESS_SAVE, resultType1);
        Assert.assertEquals(VALUE1, redis.get(this.key));
        ResultType resultType2 = redis.refreshCache(this.key, VALUE2, 60);
        Assert.assertEquals(ResultType.UNSUCCESS_CAN_NOT, resultType2);
        Assert.assertEquals(VALUE1, redis.get(this.key));
    }

    /**
     * old更新->new更新.
     */
    @Test
    public void oldUpdateNewUpdate() {
        ResultType resultType1 = redis.refreshCache(this.key, VALUE1, 60, RefreshType.UPDATE);
        Assert.assertEquals(ResultType.SUCCESS_SAVE, resultType1);
        Assert.assertEquals(VALUE1, redis.get(this.key));
        ResultType resultType2 = redis.refreshCache(this.key, VALUE2, 60, RefreshType.UPDATE);
        Assert.assertEquals(ResultType.SUCCESS_NEWER_REFRESH, resultType2);
        Assert.assertEquals(VALUE2, redis.get(this.key));
    }

    /**
     * new更新->old更新.
     */
    @Test
    public void newUpdateOldUpdate() {
        long timestamp = System.currentTimeMillis();
        ResultType resultType1 = redis.refreshCache(this.key, VALUE1, 60, RefreshType.UPDATE, timestamp, 60);
        Assert.assertEquals(ResultType.SUCCESS_SAVE, resultType1);
        Assert.assertEquals(VALUE1, redis.get(this.key));
        ResultType resultType2 = redis.refreshCache(this.key, VALUE2, 60, RefreshType.UPDATE, timestamp - 1, 60);
        Assert.assertEquals(ResultType.UNSUCCESS_CAN_NOT, resultType2);
        Assert.assertEquals(VALUE1, redis.get(this.key));
    }
}
