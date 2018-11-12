package me.lucas.kits.spi.service.impl;

import me.lucas.kits.spi.service.DemoService;
import me.lucas.kits.spi.service.InnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by zhangxin on 2018/10/31-10:28 AM.
 *
 * @author zhangxin
 * @version 1.0
 */
@Service
public class DefaultDemoServiceImpl implements DemoService {
    @Autowired
    private InnerService innerService;

    @Override
    public String say(String content) {
        return "Default say: " + innerService.say();
    }

    public class DemoClass {
        private String code;

        public void run() {

        }
    }
}
