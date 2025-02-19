package cn.kiko.net_monitor_analysis_system.doris;

import cn.kiko.net_monitor_analysis_system.collector.MonitorDataCollector;
import cn.kiko.switch_sdk.data.PacketReader;
import cn.kiko.switch_sdk.device.Packet;
import cn.kiko.switch_sdk.device.Switch;
import cn.kiko.net_monitor_analysis_system.runner.ServerStarter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

@SpringBootTest
public class TestDataGenerator {
    @Value("${collector.port}")
    private int port;

    // 不要实例化这个 bean，否则会启动服务阻塞测试
    @MockBean
    private ServerStarter starter;

    @Autowired
    private MonitorDataCollector collector;

    void waitUntilNextSec() throws InterruptedException {
        long currentTimeMillis = System.currentTimeMillis();
        long waitTime = 1000 - currentTimeMillis % 1000;
        Thread.sleep(waitTime);
    }

    @Test
    public void testDataGenerate() throws InterruptedException {
        waitUntilNextSec();
        new Thread(collector::startCollectorServer).start();
        new Thread(() -> {
            PacketReader packetReader = new PacketReader();
            List<Packet> packets = packetReader.readNPacket(100000);
            Switch switchX = new Switch("localhost", port, 1, 3, 100, 8, 1024);
            new Thread(() -> {
                for (int i = 0; i < packets.size(); i++) {
                    Packet packet = packets.get(i);
                    switchX.receivePacket(packet);
                    if (i != 0 && i % 25000 == 0) {
                        try {
                            waitUntilNextSec();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();
            new Thread(switchX::start).start();
        }).start();
        Thread.sleep(4500);
    }
}
