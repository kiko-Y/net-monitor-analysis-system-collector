package cn.kiko.net_monitor_analysis_system.device;

import cn.kiko.net_monitor_analysis_system.data.PacketReader;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SwitchSendTest {
    @Test
    public void switchSendTest() {
        String ip = "localhost";
        int port = 9400;
        PacketReader packetReader = new PacketReader();
        List<Packet> packets = packetReader.readNPacket(100000);
        Switch switchX = new Switch(ip, port, 2, 3, 100, 8, 2 * 1024 * 1024);
        new Thread(() -> {
            for (var packet: packets) {
                switchX.receivePacket(packet);
            }
        }).start();
        switchX.start();
    }
}
