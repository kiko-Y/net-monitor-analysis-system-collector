package cn.kiko.net_monitor_analysis_system.collector;

import cn.kiko.switch_sdk.algo.FlowKey;
import cn.kiko.switch_sdk.model.SwitchExportedMonitorData;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

@Component
public class MonitorDataCollector {
    // 监听端口
    @Value("${collector.port}")
    private int port;
    @Value("${nacos.discovery.ip}")
    private String hostIP;
    @Value("${nacos.discovery.port}")
    private Integer hostPort;
    private static final Logger logger = LoggerFactory.getLogger(MonitorDataCollector.class);
    private Selector selector;
    // 大小为 4 的固定大小线程池
    private ThreadPoolExecutor pool;
    @Autowired
    private KafkaTemplate<String,Long> kafkaTemplate;
    @Autowired
    @Qualifier("dorisTemplate")
    private JdbcTemplate dorisTemplate;
    @NacosValue(value = "${switch.count}", autoRefreshed = true)
    private Long switchCount;
    @NacosValue(value = "${switch.time_interval_sec}", autoRefreshed = true)
    private Long switchTimeIntervalSec;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private RedisTemplate<String,byte[]> bytesRedisTemplate;

//    @Value("${collector.kafka.topic}")
    @Value("${kafka-test.topic}")
    private String topic;

    public MonitorDataCollector() {
        pool = new ThreadPoolExecutor(4, 4, 0L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void handleClient(SelectionKey key) {
        SocketChannel clientSocketChannel = (SocketChannel)key.channel();
        // 新起了线程来处理，要先把 interested events 设为空
        key.interestOps(0);
        pool.submit(() -> {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            while (true) {
                int sz;
                try {
                    if ((sz = clientSocketChannel.read(buffer)) == -1) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                os.write(buffer.array(),0, sz);
                buffer.clear();
            }
            key.interestOps(SelectionKey.OP_READ);
            SwitchExportedMonitorData<FlowKey> monitorData = SwitchExportedMonitorData.parseFromBytes(os.toByteArray(), FlowKey.class);
            logger.info("received data : {}, size: {}, at timestamp: {}", monitorData.hashCode(), os.size(), System.currentTimeMillis());
//            dataInsertToDoris(monitorData);
            dataInsertToRedis(monitorData);
            checkAndSendToKafka(monitorData);
        });
    }

    private void checkAndSendToKafka(SwitchExportedMonitorData<FlowKey> monitorData) {
        String redisKey = generateRedisKeyForRealtimeCounter(monitorData.getTimeStamp());
        Long receivedCountForCurrentTimestamp = redisTemplate.opsForValue().increment(redisKey);
        redisTemplate.expire(redisKey, 3 * switchTimeIntervalSec, TimeUnit.SECONDS);
        if (receivedCountForCurrentTimestamp == null) {
            logger.error("get null value for redis key: {}", redisKey);
            return;
        }
        logger.info("received data count for current timestamp({}): {}/{}", monitorData.getTimeStamp(), receivedCountForCurrentTimestamp, switchCount);
        // 当前时间戳的测量数据全部收集完成，发送消息进行数据处理
        if (receivedCountForCurrentTimestamp.equals(switchCount)) {
            CompletableFuture<SendResult<String, Long>> future = kafkaTemplate.send(topic, monitorData.getTimeStamp());
            future.thenRun(() -> {
                logger.info("timestamp {} has sent to kafka", monitorData.getTimeStamp());
            });
            redisTemplate.delete(redisKey);
        }
    }

    private void dataInsertToRedis(SwitchExportedMonitorData<FlowKey> monitorData) {
        String redisKey = generateRedisKeyForRealtimeDataCacheHash(monitorData.getTimeStamp());
        logger.info("generate key: {}", redisKey);
        bytesRedisTemplate.opsForHash().put(redisKey, monitorData.getSwitchID(), monitorData.toBytes(FlowKey.class));
        bytesRedisTemplate.expire(redisKey, 3 * switchTimeIntervalSec, TimeUnit.SECONDS);
        logger.info("data cached to redis, key: {}, hashKey: {}, expire time: {}s", redisKey, monitorData.getSwitchID(), 3*switchTimeIntervalSec);
    }

    private void dataInsertToDoris(SwitchExportedMonitorData<FlowKey> monitorData) {
        // 数据上送 doris
        ObjectMapper objectMapper = new ObjectMapper();
        String sql;
        // sql example: INSERT INTO measurement_info (`type`, `timestamp`, `switch_id`, `date`, `heavy_change_keys`, `heavy_hitter_keys`, `depth_for_size_cm`, `width_for_size_cm`, `size_cm`, `depth_for_count_cm`, `width_for_count_cm`, `count_cm`) VALUES (0, 1738921137, "cfcd208495d535efa6e7dff9f98764da", "2025-02-07", [], [], 8, 32, [[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]], 8, 32, [[39,9,93,30,5,22,13,45,29,27,14,25,23,29,24,53,20,64,17,206,7,25,24,48,40,14,17,13,75,29,28,22],[17,9,91,34,36,20,10,42,17,27,20,52,18,32,23,212,13,64,8,68,17,25,29,16,44,13,13,22,73,26,27,11],[13,25,24,67,23,64,16,204,64,29,26,20,48,14,16,32,17,21,11,33,40,9,93,20,14,30,26,50,17,26,12,25],[13,26,23,43,12,31,24,10,48,14,18,28,31,25,265,7,30,14,23,28,60,14,26,8,40,18,24,93,11,23,32,67],[25,232,16,84,9,43,26,9,43,20,30,31,64,14,94,16,29,24,13,9,24,40,8,30,23,39,23,27,10,33,19,22],[21,26,12,66,29,29,12,44,21,15,46,3,268,24,53,20,19,14,37,10,26,19,39,21,28,19,34,15,23,23,10,103],[60,20,204,19,9,12,75,27,38,69,46,19,9,36,20,24,21,10,25,6,111,14,28,30,40,24,12,13,35,16,37,20],[49,13,18,13,75,29,23,41,32,63,18,207,6,23,28,64,21,26,10,29,24,30,22,36,31,8,94,28,7,21,12,28]])
        try {
            sql = String.format("INSERT INTO measurement_info " +
                            "(`type`, `timestamp`, `switch_id`, `date`, `heavy_change_keys`, `heavy_hitter_keys`, `top_k_keys`," +
                            "`depth_for_size_cm`, `width_for_size_cm`, `size_cm`, " +
                            "`depth_for_count_cm`, `width_for_count_cm`, `count_cm`) " +
                            "VALUES (0, %d, \"%s\", \"%s\", %s, %s, %s, %d, %d, %s, %d, %d, %s)",
                    monitorData.getTimeStamp(), monitorData.getSwitchID(), new SimpleDateFormat("yyyy-MM-dd").format(new Date(monitorData.getTimeStamp() * 1000)),
                    objectMapper.writeValueAsString(monitorData.getExportedMonitorData().getHeavyChangeKeys()),
                    objectMapper.writeValueAsString(monitorData.getExportedMonitorData().getHeavyHitterKeys()),
                    objectMapper.writeValueAsString(monitorData.getExportedMonitorData().getTopKKeys()),
                    monitorData.getExportedMonitorData().getDepthForSizeCM(), monitorData.getExportedMonitorData().getWidthForSizeCM(),
                    objectMapper.writeValueAsString(monitorData.getExportedMonitorData().getSizeCM()),
                    monitorData.getExportedMonitorData().getDepthForCountCM(), monitorData.getExportedMonitorData().getWidthForCountCM(),
                    objectMapper.writeValueAsString(monitorData.getExportedMonitorData().getCountCM())
            );
        } catch (JsonProcessingException e) {
            logger.error("error when serialize json");
            throw new RuntimeException(e);
        }
//            logger.info("pre execute sql:\n{}", sql);
        int result = dorisTemplate.update(sql);
        logger.info("execute sql result: {}", result);
    }

    private String generateRedisKeyForRealtimeDataCacheHash(Long timestamp) {
        return String.format("realtime:data:cache:%d", timestamp);
    }

    private String generateRedisKeyForRealtimeCounter(Long timestamp) {
        return String.format("realtime:data:counter:%d", timestamp);
    }

    public void startCollectorServer() {
        try {
            selector = Selector.open();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()){
            serverSocketChannel.bind(new InetSocketAddress(port));
            // 设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 注册 interested event
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("collector server start at container port {}", port);
            logger.info("collector server start at host {}:{}", hostIP, hostPort);
            while (true) {
                int readyCount = selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel channel = serverSocketChannel.accept();
                        if (channel == null) {
                            continue;
                        }
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        handleClient(key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
