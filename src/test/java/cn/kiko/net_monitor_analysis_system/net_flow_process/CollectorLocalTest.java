package cn.kiko.net_monitor_analysis_system.net_flow_process;

import cn.kiko.net_monitor_analysis_system.collector.MonitorDataCollector;
import cn.kiko.net_monitor_analysis_system.data.PacketReader;
import cn.kiko.net_monitor_analysis_system.device.Packet;
import cn.kiko.net_monitor_analysis_system.device.Switch;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

public class CollectorLocalTest {
    @Test
    public void collectorSimpleTest() {
        int port = 9400;
        String ip = "localhost";

        // 收集器启动
        MonitorDataCollector collector = new MonitorDataCollector();
        try {
            Field portField = MonitorDataCollector.class.getDeclaredField("port");
            portField.setAccessible(true);
            portField.set(collector, port);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Thread collectorThread = new Thread(collector::startCollectorServer);
        collectorThread.start();

        PacketReader packetReader = new PacketReader();
        List<Packet> packets = packetReader.readNPacket(100000);
        Switch switchX = new Switch(3, 100, 8, 2 * 1024 * 1024);

        for (var packet: packets) {
            switchX.receivePacket(packet);
        }
        switchX.exportToCollectorAndReset(ip, port);

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
