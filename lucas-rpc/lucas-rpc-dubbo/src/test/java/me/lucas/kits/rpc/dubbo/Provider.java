package me.lucas.kits.rpc.dubbo;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.rpc.dubbo.Provider.ProviderConfig;
import me.lucas.kits.rpc.dubbo.api.DemoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(classes = { ProviderConfig.class })
@WebAppConfiguration
public class Provider {

    @Autowired(required = false)
    private DemoService demoService;

    @Test
    public void provider() throws IOException {
        //        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"http://10.20.160.198/wiki/display/dubbo/provider.xml"});
        //        context.start();
        System.in.read(); // 按任意键退出
    }

    @Configuration
    @ImportResource(value = { "classpath:spring-properties.xml", "classpath:dubbo-demo-provider.xml"})
    @DubboComponentScan(basePackages = "me.lucas.kits.rpc.dubbo.provider")
    public static class ProviderConfig{

    }
}
