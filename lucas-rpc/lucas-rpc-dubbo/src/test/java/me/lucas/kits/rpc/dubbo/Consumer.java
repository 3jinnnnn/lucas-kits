package me.lucas.kits.rpc.dubbo;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.rpc.dubbo.Consumer.ConsumerConfig;
import me.lucas.kits.rpc.dubbo.provider.DemoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
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
@ContextConfiguration(classes = { ConsumerConfig.class })
@WebAppConfiguration
public class Consumer {

    @Reference
    private DemoService demoService;

    /**
     * 查询.
     */
    @Test
    public void select() {
        String consumer = demoService.sayHello("consumer");
        log.info(consumer);
    }

    @Configuration
    @ImportResource(value = { "classpath:spring-properties.xml", "classpath:dubbo-demo-consumer.xml" })
    @DubboComponentScan(basePackages = "me.lucas.kits.rpc.dubbo.provider")
    public static class ConsumerConfig {

    }

}
