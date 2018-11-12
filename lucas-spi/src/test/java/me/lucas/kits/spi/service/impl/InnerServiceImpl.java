package me.lucas.kits.spi.service.impl;

import me.lucas.kits.spi.service.InnerService;
import org.springframework.stereotype.Service;

/**
 * Created by zhangxin on 2018/11/12-11:10 AM.
 *
 * @author zhangxin
 * @version 1.0
 */
@Service
public class InnerServiceImpl implements InnerService {
    @Override
    public String say() {
        return "123";
    }
}
