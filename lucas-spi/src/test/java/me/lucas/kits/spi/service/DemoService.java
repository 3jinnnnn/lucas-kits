package me.lucas.kits.spi.service;

import me.lucas.kits.spi.common.SPI;

/**
 * Created by zhangxin on 2018/10/31-10:27 AM.
 *
 * @author zhangxin
 * @version 1.0
 */
//@SPI()
@SPI("default")
public interface DemoService {
    String say(String content);
}
