package cn.kiko.net_monitor_analysis_system;

import cn.kiko.net_monitor_analysis_system.device.Packet;
import cn.kiko.net_monitor_analysis_system.sender.PacketSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class NetMonitorAnalysisSystemApplicationTests {

    @Test
    void contextLoads() {
        List<Packet> packets = new PacketSender().readAllPacket();
        System.out.println(packets.size());
    }

}
