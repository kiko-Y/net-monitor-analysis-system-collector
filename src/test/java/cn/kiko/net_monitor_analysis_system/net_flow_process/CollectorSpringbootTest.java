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
    public void collectorTest() throws InterruptedException {
        new Thread(collector::startCollectorServer).start();
        new Thread(() -> {
            PacketReader packetReader = new PacketReader();
            List<Packet> packets = packetReader.readNPacket(100000);
            Switch switchX = new Switch("localhost", port, 1, 3, 100, 8, 1024);
            new Thread(() -> {
                for (int i = 0; i < packets.size(); i++) {
                    Packet packet = packets.get(i);
                    switchX.receivePacket(packet);
                    if (i != 0 && i % 3000 == 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();
            new Thread(switchX::start).start();
        }).start();
        Thread.sleep(4000);
    }
}
