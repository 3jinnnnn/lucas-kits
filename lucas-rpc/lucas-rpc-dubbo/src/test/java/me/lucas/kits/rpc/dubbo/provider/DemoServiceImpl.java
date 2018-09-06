package me.lucas.kits.rpc.dubbo.provider;

import me.lucas.kits.rpc.dubbo.provider.DemoService;
import org.springframework.stereotype.Service;

/**
 * Created by zhangxin on 2018/9/5-下午8:16.
 *
 * @author zhangxin
 * @version 1.0
 */
@Service
@com.alibaba.dubbo.config.annotation.Service(timeout = 5000)
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
