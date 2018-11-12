package me.lucas.kits.spi.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Created by zhangxin on 2018/11/12-1:28 PM.
 *
 * @author zhangxin
 * @version 1.0
 */
@Service
public class SpringContextHolder implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static final ThreadLocal<HttpServletResponse> RESPONSE_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 实现ApplicationContextAware接口的context注入函数, 将其存入静态变量.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.applicationContext = applicationContext; // NOSONAR
    }

    /**
     * 取得存储在静态变量中的ApplicationContext.
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    /**
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        checkApplicationContext();
        return (T) applicationContext.getBean(name);
    }

    /**
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return (T) applicationContext.getBeansOfType(clazz);
    }

    /**
     * 清除applicationContext静态变量.
     */
    public static void cleanApplicationContext() {
        applicationContext = null;
    }

    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException(
                    "applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
        }
    }


    public static void setHttpRequestResponseHolder(HttpServletRequest request, HttpServletResponse response){
        RESPONSE_THREAD_LOCAL.set(response);
        ApplicationContext ap = WebApplicationContextUtils.getWebApplicationContext(null);
    }
    public static HttpServletResponse getHttpResponse(){
        return RESPONSE_THREAD_LOCAL.get();
    }

    public static void clean(){
        RESPONSE_THREAD_LOCAL.remove();
    }

}
