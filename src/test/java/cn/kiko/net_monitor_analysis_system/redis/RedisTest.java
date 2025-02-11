package cn.kiko.net_monitor_analysis_system.redis;

import cn.kiko.net_monitor_analysis_system.runner.ServerStarter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class RedisTest {
    // 不要实例化这个 bean，否则会启动服务阻塞测试
    @MockBean
    private ServerStarter starter;

    private static final Logger logger = LoggerFactory.getLogger(RedisTest.class);

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private final String testKey = "test:key1";
    @Test
    public void redisTest() throws InterruptedException {
        int count = 5;
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; ++i) {
            new Thread(() -> {
                for (int j = 0; j < 5000; ++j) {
                    redisTemplate.opsForValue().increment(testKey, 1);
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        logger.info("get key, result: {}", redisTemplate.opsForValue().get(testKey));
        logger.info("increment key, result: {}", redisTemplate.opsForValue().increment(testKey));
        logger.info("deleted: {}", redisTemplate.delete(testKey));
        logger.info("deleted: {}", redisTemplate.delete(testKey));
    }
}
