package cn.kiko.net_monitor_analysis_system.redis;

import cn.kiko.net_monitor_analysis_system.runner.ServerStarter;
import cn.kiko.switch_sdk.algo.FlowKey;
import cn.kiko.switch_sdk.model.SwitchExportedMonitorData;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class RedisTest {
    // 不要实例化这个 bean，否则会启动服务阻塞测试
    @MockBean
    private ServerStarter starter;

    private static final Logger logger = LoggerFactory.getLogger(RedisTest.class);

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private RedisTemplate<String,byte[]> bytesRedisTemplate;

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

    @Test
    public void redisBytesTemplateTest() {
        bytesRedisTemplate.opsForValue().set("1", new byte[]{1, 2, 3});
        byte[] bytes = bytesRedisTemplate.opsForValue().get("1");
        System.out.println(Arrays.toString(bytes));
        bytesRedisTemplate.delete("1");
    }

    @Test
    public void redisHashTest() {
        String key = "tempKey";
        redisTemplate.opsForHash().put(key, "1", "a");
        redisTemplate.opsForHash().put(key, "2", "b");
        System.out.println(redisTemplate.opsForHash().entries(key));
        redisTemplate.delete(key);
    }

    @Test
    public void redisDataTest() {
//        bytesRedisTemplate.delete("realtime:data:cache:1740027735");
        Set<String> keys = bytesRedisTemplate.keys("realtime:data:cache:*");
        System.out.println(keys);
        Map<Object, Object> entries = bytesRedisTemplate.opsForHash().entries("realtime:data:cache:1740028565");
        System.out.println(entries.keySet());
//        byte[] bytes = (byte[]) entries.values().stream().findFirst().orElseThrow();
//        SwitchExportedMonitorData<FlowKey> data = SwitchExportedMonitorData.<FlowKey>parseFromBytes(bytes, FlowKey.class);
//        System.out.println(data.getSwitchID() + " " + data.getTimeStamp());
    }
}
