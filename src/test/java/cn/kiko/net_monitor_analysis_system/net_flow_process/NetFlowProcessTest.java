package cn.kiko.net_monitor_analysis_system.net_flow_process;

import cn.kiko.net_monitor_analysis_system.algo.FlowKey;
import cn.kiko.net_monitor_analysis_system.algo.FlowStatisticAlgo;
import cn.kiko.net_monitor_analysis_system.data.PacketReader;
import cn.kiko.net_monitor_analysis_system.device.Packet;
import cn.kiko.net_monitor_analysis_system.device.Switch;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class NetFlowProcessTest {
    @Test
    public void netFlowProcessTest() {
        PacketReader packetReader = new PacketReader();
        List<Packet> packets = packetReader.readNPacket(100000);
        Switch switchX = new Switch(3, 100, 8, 2 * 1024 * 1024);
        HashMap<Packet, Integer> map = new HashMap<>();
        for (Packet packet: packets) {
            switchX.receivePacket(packet);
            map.merge(packet, 1, Integer::sum);
        }
        FlowStatisticAlgo<FlowKey> flowStatisticAlgo = switchX.getFlowStatisticAlgo();
        flowStatisticAlgo.clearCache();
        System.out.println(map.size());
        List<Packet> list = map.keySet().stream().filter(x -> map.get(x) > 100)
                .sorted(Comparator.comparingInt(map::get))
                .toList();
        System.out.println(list.size());
        for (var packet: list) {
            int realCount = map.get(packet);
            int estimatedCount = flowStatisticAlgo.getCmCount().query(Switch.packetToFlowKey(packet));
            System.out.print("Real Value: " + realCount + ", Estimate Value: " + estimatedCount);
            System.out.println(realCount == estimatedCount ? "" : " *");
        }
    }
}
