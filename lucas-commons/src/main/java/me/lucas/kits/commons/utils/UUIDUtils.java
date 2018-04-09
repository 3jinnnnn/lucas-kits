package me.lucas.kits.commons.utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.LoggerFactory;

/**
 * Created by zhangxin on 2018/3/12-上午11:36.
 *
 * @author zhangxin
 * @version 1.0
 */
public class UUIDUtils {
    private static boolean IS_THREADLOCALRANDOM_AVAILABLE = false;
    private static Random random;
    private static final long LEAST_SIG_BITS;
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static AtomicLong lastTime = new AtomicLong(0);
    private static final int INT8 = 8;
    private static final int INT16 = 16;
    private static final int INT20 = 20;

    static {
        try {
            IS_THREADLOCALRANDOM_AVAILABLE =
                    null != UUIDUtils.class.getClassLoader().loadClass("java.util.concurrent.ThreadLocalRandom");
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(UUIDUtils.create()).warn("[java.util.concurrent.ThreadLocalRandom] is not found, now use Random instead");
        }

        byte[] seed = new SecureRandom().generateSeed(8);
        LEAST_SIG_BITS = new BigInteger(seed).longValue();
        if (!IS_THREADLOCALRANDOM_AVAILABLE) {
            random = new Random(LEAST_SIG_BITS);
        }
    }

    private UUIDUtils() {
    }

    /**
     * Create a new random UUID.
     *
     * @return the new UUID
     */
    public static String random() {
        byte[] randomBytes = new byte[16];
        if (IS_THREADLOCALRANDOM_AVAILABLE) {
            java.util.concurrent.ThreadLocalRandom.current().nextBytes(randomBytes);
        } else {
            random.nextBytes(randomBytes);
        }

        long mostSigBits = 0;
        for (int i = 0; i < INT8; i++) {
            mostSigBits = (mostSigBits << INT8) | (randomBytes[i] & 0xff);
        }
        long leastSigBits = 0;
        for (int i = INT8; i < INT16; i++) {
            leastSigBits = (leastSigBits << 8) | (randomBytes[i] & 0xff);
        }

        return new UUID(mostSigBits, leastSigBits).toString().replaceAll("-", "");
    }

    /**
     * Create a new time-based UUID.
     *
     * @return the new UUID
     */
    public static String create() {
        long timeMillis = (System.currentTimeMillis() * 10000) + 0x01B21DD213814000L;

        LOCK.lock();
        try {
            if (timeMillis > lastTime.get()) {
                lastTime.set(timeMillis);
            } else {
                timeMillis = lastTime.incrementAndGet();
            }
        } finally {
            LOCK.unlock();
        }

        // time low
        long mostSigBits = timeMillis << 32;

        // time mid
        mostSigBits |= (timeMillis & 0xFFFF00000000L) >> 16;

        // time hi and version
        // version 1
        mostSigBits |= 0x1000 | ((timeMillis >> 48) & 0x0FFF);

        return new UUID(mostSigBits, LEAST_SIG_BITS).toString().replaceAll("-", "");
    }

    public static void main(String[] args) {
        for (int i = 0; i < INT20; i++) {
            System.out.println(UUIDUtils.random());
        }
        //        System.out.println(UUIDUtils.create());
    }
}
