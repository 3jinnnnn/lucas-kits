package me.lucas.kits.spi.service.impl;

import me.lucas.kits.spi.service.DemoService;

/**
 * Created by zhangxin on 2018/10/31-10:28 AM.
 *
 * @author zhangxin
 * @version 1.0
 */
public class SecondDemoServiceImpl implements DemoService {
    @Override
    public String say(String content) {
        return "Second say: " + content;
    }
}
