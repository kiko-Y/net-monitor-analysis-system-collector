package cn.kiko.net_monitor_analysis_system.device;

import cn.kiko.net_monitor_analysis_system.algo.FlowKey;
import cn.kiko.net_monitor_analysis_system.algo.FlowStatisticAlgo;
import cn.kiko.net_monitor_analysis_system.algo.value.MonitorValue;
import cn.kiko.net_monitor_analysis_system.model.SwitchExportedMonitorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Switch {
    private static int switchIDSeed = 0;
    private FlowStatisticAlgo<FlowKey> flowStatisticAlgo1;
    private FlowStatisticAlgo<FlowKey> flowStatisticAlgo2;
    private final String switchID;
    private final Lock locker = new ReentrantLock();
    private String destIP;
    private int destPort;
    private long intervalInSec;
    private static final Logger logger = LoggerFactory.getLogger(Switch.class);

    public Switch(String destIP, int destPort, long intervalInSec, int lruSize, int maxBucketNum, int sketchDepth, int sketchMemoryInBytes) {
        this.destIP = destIP;
        this.destPort = destPort;
        this.intervalInSec = intervalInSec;
        this.switchID = UUID.nameUUIDFromBytes(String.valueOf(switchIDSeed++).getBytes()).toString().replace("-", "");
        this.flowStatisticAlgo1 = FlowStatisticAlgo.<FlowKey>builder()
                .lruSize(lruSize)
                .maxBucketNum(maxBucketNum)
                .sketchDepth(sketchDepth)
                .sketchMemoryInBytes(sketchMemoryInBytes)
                .build();
        this.flowStatisticAlgo2 = FlowStatisticAlgo.<FlowKey>builder()
                .lruSize(lruSize)
                .maxBucketNum(maxBucketNum)
                .sketchDepth(sketchDepth)
                .sketchMemoryInBytes(sketchMemoryInBytes)
                .build();
    }

    public Switch(String destIP, int destPort, long intervalInSec, int lruSize, int maxBucketNum, int sketchDepth, int sketchMemoryInBytes,
                  double sampleRate, int sampleThreshold,
                  int heavyChangeThreshold, int heavyHitterThreshold,
                  int k) {
        this.destIP = destIP;
        this.destPort = destPort;
        this.intervalInSec = intervalInSec;
        this.switchID = UUID.nameUUIDFromBytes(String.valueOf(switchIDSeed++).getBytes()).toString().replace("-", "");
        this.flowStatisticAlgo1 = new FlowStatisticAlgo<>(lruSize, maxBucketNum, sketchDepth, sketchMemoryInBytes,
                sampleRate, sampleThreshold,
                heavyChangeThreshold, heavyHitterThreshold,
                k);
        this.flowStatisticAlgo2 = new FlowStatisticAlgo<>(lruSize, maxBucketNum, sketchDepth, sketchMemoryInBytes,
                sampleRate, sampleThreshold,
                heavyChangeThreshold, heavyHitterThreshold,
                k);
    }

    public FlowStatisticAlgo<FlowKey> getFlowStatisticAlgo() {
        return flowStatisticAlgo1;
    }

    public static FlowKey packetToFlowKey(Packet packet) {
        FlowKey fk = new FlowKey();
        BeanUtils.copyProperties(packet, fk);
        return fk;
    }

    public void receivePacket(Packet packet) {
        locker.lock();
        try {
            flowStatisticAlgo1.receiveKV(packetToFlowKey(packet), new MonitorValue(0, 1));
        } finally {
            locker.unlock();
        }
    }

    public void start() {
        long currentTimeInMill = System.currentTimeMillis();
        long waitTime = intervalInSec * 1000 - currentTimeInMill % (intervalInSec * 1000);
        while (true) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            exportToCollectorAndReset(destIP, destPort);
            currentTimeInMill = System.currentTimeMillis();
            waitTime = intervalInSec * 1000 - currentTimeInMill % (intervalInSec * 1000);
        }
    }

    // 导出流统计数据并重置数据结构
    public void exportToCollectorAndReset(String ip, int port) {
        // lock and swap
        locker.lock();
        try {
            // swap
            FlowStatisticAlgo<FlowKey> temp = flowStatisticAlgo1;
            flowStatisticAlgo1 = flowStatisticAlgo2;
            flowStatisticAlgo2 = temp;
        } finally {
            locker.unlock();
        }
        long timeStamp = System.currentTimeMillis() / 1000;
        SwitchExportedMonitorData<FlowKey> switchExportedMonitorData = new SwitchExportedMonitorData<>(switchID, timeStamp, flowStatisticAlgo2.exportAndReset());
        byte[] byteData = switchExportedMonitorData.toBytes(FlowKey.class);
        logger.info("Switch {} send data: {}, at timeStamp: {}, size: {}", switchID, switchExportedMonitorData.hashCode(), timeStamp, byteData.length);
        try (Socket socket = new Socket(ip, port)) {
            socket.getOutputStream().write(byteData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SwitchExportedMonitorData<FlowKey> exportDataStructureAndReset() {
        // lock and swap
        locker.lock();
        try {
            // swap
            FlowStatisticAlgo<FlowKey> temp = flowStatisticAlgo1;
            flowStatisticAlgo1 = flowStatisticAlgo2;
            flowStatisticAlgo2 = temp;
        } finally {
            locker.unlock();
        }
        return new SwitchExportedMonitorData<>(switchID, System.currentTimeMillis() / 1000, flowStatisticAlgo2.exportAndReset());
    }
}
