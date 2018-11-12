package me.lucas.kits.spi;

import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.spi.SpiTest.Config;
import me.lucas.kits.spi.common.ExtensionLoader;
import me.lucas.kits.spi.service.DemoService;
import me.lucas.kits.spi.utils.SpringContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import redis.clients.jedis.Protocol;

/**
 * Created by zhangxin on 2018/10/31-10:23 AM.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
//此注解用来加载配置ApplicationContext
@ContextConfiguration(classes = { Config.class, SpringContextHolder.class })
@WebAppConfiguration
public class SpiTest {

    @Test
    public void test() {
        try {
            Class<?> aClass = Class.forName("me.lucas.kits.spi.service.impl.DefaultDemoServiceImpl$DemoClass");
            System.out.println(123);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        ;
        boolean equals = new Integer(1).equals(null);
        try {
            DemoService demoService1 = ExtensionLoader.getExtensionLoader(DemoService.class).getExtension("first");
            log.info(demoService1.say("first"));

            DemoService demoService2 = ExtensionLoader.getExtensionLoader(DemoService.class).getExtension("second");
            log.info(demoService2.say("second"));

            DemoService demoService3 = ExtensionLoader.getExtensionLoader(DemoService.class).getDefaultExtension();
            log.info(demoService3.say("unknow"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Configuration
    @ImportResource(value = { "classpath:spring-properties.xml" })
    @ComponentScan()
    public static class Config {

    }
}
