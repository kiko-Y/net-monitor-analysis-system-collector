package cn.kiko.net_monitor_analysis_system.device;

import cn.kiko.net_monitor_analysis_system.algo.FlowKey;
import cn.kiko.net_monitor_analysis_system.algo.FlowStatisticAlgo;
import cn.kiko.net_monitor_analysis_system.algo.value.MonitorValue;
import cn.kiko.net_monitor_analysis_system.model.ExportedMonitorData;
import org.springframework.beans.BeanUtils;

public class Switch {
    FlowStatisticAlgo<FlowKey> flowStatisticAlgo;

    public Switch(int lruSize, int maxBucketNum, int sketchDepth, int sketchMemoryInBytes) {
        this.flowStatisticAlgo = FlowStatisticAlgo.<FlowKey>builder()
                .lruSize(lruSize)
                .maxBucketNum(maxBucketNum)
                .sketchDepth(sketchDepth)
                .sketchMemoryInBytes(sketchMemoryInBytes)
                .build();
    }

    public Switch(int lruSize, int maxBucketNum, int sketchDepth, int sketchMemoryInBytes,
                  double sampleRate, int sampleThreshold,
                  int heavyChangeThreshold, int heavyHitterThreshold,
                  int k) {
        this.flowStatisticAlgo = new FlowStatisticAlgo<>(lruSize, maxBucketNum, sketchDepth, sketchMemoryInBytes,
                sampleRate, sampleThreshold,
                heavyChangeThreshold, heavyHitterThreshold,
                k);
    }

    public FlowStatisticAlgo<FlowKey> getFlowStatisticAlgo() {
        return flowStatisticAlgo;
    }

    public static FlowKey packetToFlowKey(Packet packet) {
        FlowKey fk = new FlowKey();
        BeanUtils.copyProperties(packet, fk);
        return fk;
    }

    public void receivePacket(Packet packet) {
        flowStatisticAlgo.receiveKV(packetToFlowKey(packet), new MonitorValue(0, 1));
    }

    // 导出流统计数据并重置数据结构
    public void exportToCollectorAndReset() {
        ExportedMonitorData<FlowKey> monitorData = flowStatisticAlgo.exportAndReset();
        // TODO(kiko): 数据转为 bytes 数组之后传给 collector
    }

    public ExportedMonitorData<FlowKey> exportDataStructureAndReset() {
        return flowStatisticAlgo.exportAndReset();
    }
}
