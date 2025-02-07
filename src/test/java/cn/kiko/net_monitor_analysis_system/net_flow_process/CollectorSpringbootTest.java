package cn.kiko.net_monitor_analysis_system.net_flow_process;

import cn.kiko.net_monitor_analysis_system.collector.MonitorDataCollector;
import cn.kiko.net_monitor_analysis_system.data.PacketReader;
import cn.kiko.net_monitor_analysis_system.device.Packet;
import cn.kiko.net_monitor_analysis_system.device.Switch;
import cn.kiko.net_monitor_analysis_system.runner.ServerStarter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.concurrent.ExecutionException;

@SpringBootTest
public class CollectorSpringbootTest {


    @Value("${collector.port}")
    private int port;

    // 不要实例化这个 bean，否则会启动服务阻塞测试
    @MockBean
    private ServerStarter starter;

    @Autowired
    private MonitorDataCollector collector;

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    @Test
    public void collectorTest() throws InterruptedException, ExecutionException {
        new Thread(() -> {
            PacketReader packetReader = new PacketReader();
            List<Packet> packets = packetReader.readNPacket(10000);
            Switch switchX = new Switch("localhost", port, 1, 3, 100, 8, 1024);
            new Thread(() -> {
                for (var packet : packets) {
                    switchX.receivePacket(packet);
                }
            }).start();
            new Thread(switchX::start).start();
        }).start();
        new Thread(collector::startCollectorServer).start();
        Thread.sleep(4000);
    }
}
