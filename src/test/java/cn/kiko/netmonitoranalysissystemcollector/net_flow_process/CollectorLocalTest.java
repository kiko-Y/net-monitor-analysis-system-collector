package cn.kiko.netmonitoranalysissystemcollector.net_flow_process;

import cn.kiko.netmonitoranalysissystemcollector.collector.MonitorDataCollector;
import cn.kiko.switch_sdk.algo.FlowKey2Tuple;
import cn.kiko.switch_sdk.algo.FlowKey5Tuple;
import cn.kiko.switch_sdk.data.PacketReader;
import cn.kiko.switch_sdk.device.Packet;
import cn.kiko.switch_sdk.device.Switch;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

public class CollectorLocalTest {

    @Test
    public void collectorSimpleTest() {
        int port = 9400;
        String ip = "localhost";

        // 收集器启动
        MonitorDataCollector<FlowKey2Tuple> collector = new MonitorDataCollector<>(FlowKey2Tuple.class);
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
        Switch<FlowKey2Tuple> switchX = new Switch<>(ip, port, 1, 3, 100, 8, 2 * 1024 * 1024, FlowKey2Tuple.class);
        new Thread(() -> {
            for (var packet: packets) {
                switchX.receivePacket(packet);
            }
        }).start();

        new Thread(switchX::start).start();

        try {
            Thread.sleep(4000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
