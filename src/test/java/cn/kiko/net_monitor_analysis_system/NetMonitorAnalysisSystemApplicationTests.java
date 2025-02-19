package cn.kiko.net_monitor_analysis_system;

import cn.kiko.switch_sdk.device.Packet;
import cn.kiko.switch_sdk.data.PacketReader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class NetMonitorAnalysisSystemApplicationTests {

    @Test
    void contextLoads() {
        List<Packet> packets = new PacketReader().readAllPacket();
        System.out.println(packets.size());
    }
}
