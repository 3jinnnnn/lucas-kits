package me.lucas.kits.spi.utils;

/**
 * Helper Class for hold a value.
 * Created by zhangxin on 2018/10/30-8:06 PM.
 *
 * @author zhangxin
 * @version 1.0
 */
public class Holder<T> {
    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
