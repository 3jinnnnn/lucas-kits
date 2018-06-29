package me.lucas.kits.orm.redis.redission.config;

import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

/**
 * Created by zhangxin on 2018/6/28-下午4:37.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient config() {
        try (InputStream resource = ClassUtils.getDefaultClassLoader().getResourceAsStream("redisson.yml")) {
            Config config = Config.fromYAML(resource);
            return Redisson.create(config);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
