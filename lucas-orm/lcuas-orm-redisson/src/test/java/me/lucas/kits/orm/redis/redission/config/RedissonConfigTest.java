package me.lucas.kits.orm.redis.redission.config;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import me.lucas.kits.orm.redis.redission.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by zhangxin on 2018/6/28-下午4:52.
 *
 * @author zhangxin
 * @version 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RedissonConfig.class, TestConfig.class })
public class RedissonConfigTest {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(100);
    private static final Integer THREAD_NUMS = 1000;
    @Autowired
    private RedissonClient redisson;

    @Test
    public void config() {
        Assert.assertNotNull(redisson);
    }

    @Test
    public void reentrantLock() throws InterruptedException, ExecutionException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Future<Boolean>> futures = Lists.newArrayList();
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUMS);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < THREAD_NUMS; i++) {
            Future<Boolean> future = POOL.submit(new LockTask(i, redisson, countDownLatch));
            futures.add(future);
        }
        boolean await = countDownLatch.await(60, TimeUnit.SECONDS);
        if (await) {
            long consumeTime = System.currentTimeMillis() - startTime;
            log.info("总耗时:{}ms", consumeTime);
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successCount.addAndGet(1);
                } else {
                    failCount.addAndGet(1);
                }
            }
        }
        log.info("成功/失败: {}/{}", successCount, failCount);
    }

    private static class LockTask implements Callable<Boolean> {
        private Integer i;
        private RedissonClient redisson;
        private CountDownLatch countDownLatch;

        public LockTask(Integer i, RedissonClient redisson, CountDownLatch countDownLatch) {
            this.i = i;
            this.redisson = redisson;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public Boolean call() throws Exception {
            RLock lock = redisson.getLock("anyLock");
            boolean flag11 = lock.tryLock(1000, 10000, TimeUnit.MILLISECONDS);
            try {
                if (flag11) {
                    log.info("{}获取锁[成功]", i);
                    lock.unlock();
                    return Boolean.TRUE;
                } else {
                    log.info("{}获取锁[失败]", i);
                    return Boolean.FALSE;
                }
            } catch (final Exception e) {
                log.error(e.getMessage(), e);
                return Boolean.FALSE;
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}
