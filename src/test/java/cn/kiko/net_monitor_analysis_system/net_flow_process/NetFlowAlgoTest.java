package cn.kiko.net_monitor_analysis_system.net_flow_process;

import cn.kiko.net_monitor_analysis_system.algo.FlowKey;
import cn.kiko.net_monitor_analysis_system.algo.FlowStatisticAlgo;
import cn.kiko.net_monitor_analysis_system.data.PacketReader;
import cn.kiko.net_monitor_analysis_system.device.Packet;
import cn.kiko.net_monitor_analysis_system.device.Switch;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class NetFlowAlgoTest {
    @Test
    public void netFlowProcessTest() {
        PacketReader packetReader = new PacketReader();
        List<Packet> packets = packetReader.readNPacket(100000);
        Switch switchX = new Switch("localhost", 9400, 100, 3, 100, 8, 2 * 1024 * 1024);
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

    @Test
    public void sketchMergePrecisionTest() {
        int mergeCount = 6;
        int packetsPerTimeWindow = 10000;
        int sketchMemoryInBytes = 1024 * 1024;
        int k = 10;
        PacketReader packetReader = new PacketReader();
        List<Packet> packets = packetReader.readNPacket(mergeCount * packetsPerTimeWindow);
        HashMap<Packet, Integer> realCounter = new HashMap<>();
        Switch singleSwitch = new Switch("localhost", 9400, 100, 3, 100, 8, sketchMemoryInBytes);
        Switch[] switches = new Switch[mergeCount];
        for (int i = 0; i < mergeCount; i++) {
            switches[i] = new Switch("localhost", 9400, 100, 3, 100, 8, sketchMemoryInBytes);
        }
        for (int i = 0; i < mergeCount * packetsPerTimeWindow; i++) {
            Packet packet = packets.get(i);
            singleSwitch.receivePacket(packet);
            switches[i % mergeCount].receivePacket(packet);
            realCounter.merge(packet, 1, Integer::sum);
        }
        List<Packet> topK = realCounter.keySet().stream()
                .sorted(Comparator.comparingInt(kk -> -realCounter.get(kk)))
                .limit(k)
                .toList();
        FlowStatisticAlgo<FlowKey> singleSAlgo = singleSwitch.getFlowStatisticAlgo();
        List<FlowStatisticAlgo<FlowKey>> algos = Arrays.stream(switches).map(Switch::getFlowStatisticAlgo).toList();
        singleSAlgo.clearCache();
        algos.forEach(FlowStatisticAlgo::clearCache);

        for (int i = 0; i < k; ++i) {
            Packet packet = topK.get(i);
            int estimatedValueMerge = singleSAlgo.getCmCount().query(Switch.packetToFlowKey(packet));
            int estimatedValueNoMerge = 0;
            for (int j = 0; j < mergeCount; j++) {
                estimatedValueNoMerge += algos.get(j).getCmCount().query(Switch.packetToFlowKey(packet));
            }
            int realValue = realCounter.get(packet);
            System.out.println("RealValue: " +  realValue+
                    ", EstimatedValue No Merge: " + estimatedValueNoMerge +
                    ", EstimatedValue Merge: " + estimatedValueMerge);
            System.out.printf("EstimatedValue No Merge Err: %.2f%%, EstimatedValue Merge Err: %.2f%%\n",
                    100. * (estimatedValueNoMerge - realValue) / realValue, 100. * (estimatedValueMerge - realValue) / realValue);
        }
    }
}
