package cn.kiko.net_monitor_analysis_system.algo;


import cn.kiko.net_monitor_analysis_system.algo.value.MonitorValue;
import cn.kiko.net_monitor_analysis_system.model.ExportedMonitorData;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Getter
public class FlowStatisticAlgo<Key extends IFlowKey> {
    private static final int DEFAULT_HEAVY_CHANGE_THRESHOLD = 500;
    private static final int DEFAULT_HEAVY_HITTER_THRESHOLD = 1000;
    private static final int DEFAULT_K = 10;
    private static final double DEFAULT_SAMPLE_RATE = 1.;
    private static final int DEFAULT_SAMPLE_THRESHOLD = 50;
    private P4LRU<Key, MonitorValue> p4lru;
    // 记录流频数
    private CMSketch<Key> cmCount;
    // 记录流大小
    private CMSketch<Key> cmSize;
    private double sampleRate;
    // 流频数阈值
    private int sampleThreshold;
    private int heavyChangeThreshold;
    private int heavyHitterThreshold;
    private int k;
    private HashSet<Key> heavyChangeHashTable;
    private HashSet<Key> heavyHitterHashTable;
    // TODO(kiko): TOP-K 待实现

    public FlowStatisticAlgo(int lruSize, int maxBucketNum, int sketchDepth, int sketchMemoryInBytes,
                             double sampleRate, int sampleThreshold,
                             int heavyChangeThreshold, int heavyHitterThreshold,
                             int k) {
        this.p4lru = new P4LRU<>(lruSize, maxBucketNum);
        this.cmCount = new CMSketch<>(sketchDepth, sketchMemoryInBytes);
        this.cmSize = new CMSketch<>(sketchDepth, sketchMemoryInBytes);
        this.sampleRate = sampleRate;
        this.sampleThreshold = sampleThreshold;
        this.heavyChangeThreshold = heavyChangeThreshold;
        this.heavyChangeHashTable = new HashSet<>();
        this.heavyHitterThreshold = heavyHitterThreshold;
        this.heavyHitterHashTable = new HashSet<>();
        this.k = k;
    }

    private FlowStatisticAlgo(FlowStatisticAlgoBuilder<Key> builder) {
        this(builder.lruSize, builder.maxBucketNum, builder.sketchDepth, builder.sketchMemoryInBytes,
                builder.sampleRate, builder.sampleThreshold,
                builder.heavyChangeThreshold, builder.heavyHitterThreshold,
                builder.k);
    }

    private void insertEvictedItemToSketch(Pair<Key, MonitorValue> evicted) {
        if (evicted == null) {
            return;
        }
        // 大流直接插入
        if (evicted.getRight().getCount() > this.sampleThreshold) {
            cmSize.insert(evicted.getLeft(), evicted.getRight().getSize());
            cmCount.insert(evicted.getLeft(), evicted.getRight().getCount());
        } else {
            // 小流按一定阈值插入
            if ((evicted.getLeft().hashCode() % 10000 + 10000) % 10000 < 10000 * this.sampleRate) {
                cmSize.insert(evicted.getLeft(), evicted.getRight().getSize());
                cmCount.insert(evicted.getLeft(), evicted.getRight().getCount());
            }
        }
        if (cmCount.query(evicted.getLeft()) > heavyHitterThreshold) {
            heavyHitterHashTable.add(evicted.getLeft());
        }
        if (cmCount.query(evicted.getLeft()) > heavyChangeThreshold) {
            heavyChangeHashTable.add(evicted.getLeft());
        }
    }

    public void receiveKV(Key key, MonitorValue value) {
        Pair<Key, MonitorValue> evicted = p4lru.receiveKV(key, value);
        insertEvictedItemToSketch(evicted);
    }

    public void clearCache() {
        p4lru.evictAll().forEach(this::insertEvictedItemToSketch);
    }

    public void reset() {
        p4lru.reset();
        cmSize.reset();
        cmCount.reset();
        heavyChangeHashTable.clear();
        heavyHitterHashTable.clear();
    }

    // 导出数据结构
    public ExportedMonitorData<Key> export() {
        clearCache();
        return new ExportedMonitorData<Key>(
                heavyChangeHashTable.stream().toList(),
                heavyHitterHashTable.stream().toList(),
                cmSize.getD(),
                cmCount.getD(),
                Arrays.stream(cmSize.getCounters()).map(arr -> Arrays.stream(arr).boxed().toList()).toList(),
                cmSize.getW(),
                cmCount.getW(),
                Arrays.stream(cmCount.getCounters()).map(arr -> Arrays.stream(arr).boxed().toList()).toList());
    }

    public ExportedMonitorData<Key> exportAndReset() {
        ExportedMonitorData<Key> exportedData = export();
        reset();
        return exportedData;
    }

    public static <K extends IFlowKey> FlowStatisticAlgoBuilder<K> builder() {
        return new FlowStatisticAlgoBuilder<>();
    }

    public static final class FlowStatisticAlgoBuilder<Key extends IFlowKey> {
        private int lruSize;
        private int maxBucketNum;
        private int sketchDepth;
        private int sketchMemoryInBytes;
        private double sampleRate = DEFAULT_SAMPLE_RATE;
        private int sampleThreshold = DEFAULT_SAMPLE_THRESHOLD;
        private int heavyChangeThreshold = DEFAULT_HEAVY_CHANGE_THRESHOLD;
        private int heavyHitterThreshold = DEFAULT_HEAVY_HITTER_THRESHOLD;
        private int k = DEFAULT_K;

        public FlowStatisticAlgoBuilder() {
        }

        public FlowStatisticAlgoBuilder<Key> lruSize(int val) {
            lruSize = val;
            return this;
        }
        public FlowStatisticAlgoBuilder<Key> maxBucketNum(int val) {
            maxBucketNum = val;
            return this;
        }
        public FlowStatisticAlgoBuilder<Key> sketchDepth(int val) {
            sketchDepth = val;
            return this;
        }
        public FlowStatisticAlgoBuilder<Key> sketchMemoryInBytes(int val) {
            sketchMemoryInBytes = val;
            return this;
        }

        public FlowStatisticAlgoBuilder<Key> sampleRate(double val) {
            sampleRate = val;
            return this;
        }

        public FlowStatisticAlgoBuilder<Key> sampleThreshold(int val) {
            sampleThreshold = val;
            return this;
        }

        public FlowStatisticAlgoBuilder<Key> heavyChangeThreshold(int val) {
            heavyChangeThreshold = val;
            return this;
        }

        public FlowStatisticAlgoBuilder<Key> heavyHitterThreshold(int val) {
            heavyHitterThreshold = val;
            return this;
        }

        public FlowStatisticAlgoBuilder<Key> k(int val) {
            k = val;
            return this;
        }

        public FlowStatisticAlgo<Key> build() {
            return new FlowStatisticAlgo<>(this);
        }
    }
}
