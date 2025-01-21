package cn.kiko.net_monitor_analysis_system.collector;

import cn.kiko.net_monitor_analysis_system.algo.FlowKey;
import cn.kiko.net_monitor_analysis_system.model.ExportedMonitorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

@Component
public class MonitorDataCollector {
    // 监听端口
    @Value("${collector.port}")
    private int port;
    private static final Logger logger = LoggerFactory.getLogger(MonitorDataCollector.class);
    private Selector selector;
    // 大小为 4 的固定大小线程池
    private ThreadPoolExecutor pool;
    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

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
                    // TODO(kiko): 目前是读完，直到对端关闭，后续考虑实现成为长连接，需要进行额外处理
                    if ((sz = clientSocketChannel.read(buffer)) == -1) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                os.write(buffer.array(),0, sz);
                buffer.clear();
            }
            key.interestOps(SelectionKey.OP_READ);
            ExportedMonitorData<FlowKey> monitorData = ExportedMonitorData.parseFromBytes(os.toByteArray(), FlowKey.class);
            logger.info("received data: " + monitorData.hashCode());
            // TODO(kiko): kafka 接收消息的默认大小是 1M，需要进行修改
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, monitorData);
            future.thenRun(() -> {
                logger.info("message {} has sent to kafka", monitorData.hashCode());
            });
        });
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
            logger.info("server start at port {}", port);
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
