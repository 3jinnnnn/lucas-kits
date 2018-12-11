package me.lucas.kits.orm.redis.jedis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.orm.redis.jedis.config.RedisClientConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by zhangxin on 2018/12/10-8:38 PM.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
//此注解用来加载配置ApplicationContext
@ContextConfiguration(classes = { RedisClientConfig.class, SpringConfig.class })
@WebAppConfiguration
public class HashRedisClientTest {
    @Autowired
    @Qualifier(RedisClientConfig.REDIS_PRIFIX + "main")
    private RedisClient redis;

    @Test
    public void hmset() {
        Map<String, Object> map = Stream.iterate(1, x -> x + 1).limit(40).map(UserVerifyRecord::new)
                .collect(Collectors.toMap(x -> String.valueOf(x.getProductId()), y -> y));
        redis.hmset("HMSET_KEY", map);
    }

    @Test
    public void hmsetEX() {
        Map<String, Object> map = Stream.iterate(2, x -> x + 1).limit(40).map(UserVerifyRecord::new)
                .collect(Collectors.toMap(x -> String.valueOf(x.getProductId()), y -> y));
        redis.hmset("HMSET_KEY", map, 3600);
    }

    @Test
    public void hgetall() {
        Map<String, UserVerifyRecord> map = redis.hgetAll("HMSET_KEY", new TypeReference<UserVerifyRecord>() {
        });
        log.info(JSON.toJSONString(map));
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class UserVerifyRecord {
        private static final String LM_MEMBER_ID = "123";
        private String lmMemberId;
        private Integer productId;
        private Date operateTime;

        public UserVerifyRecord() {
            this.productId = 0;
            this.lmMemberId = LM_MEMBER_ID;
            this.operateTime = new Date();
        }

        public UserVerifyRecord(Integer productId) {
            this.productId = productId;
            this.lmMemberId = LM_MEMBER_ID;
            this.operateTime = new Date();
        }
    }
}
