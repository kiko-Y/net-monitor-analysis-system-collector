package cn.kiko.net_monitor_analysis_system.algo;

import lombok.Getter;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

@Getter
public class CMSketch<Key extends IFlowKey> {
    private int d;
    private int memoryInBytes;
    private int w;
    private int[][] counters;
    private Function<Key, Integer>[] hashFunctions;

    @SuppressWarnings("unchecked")
    public CMSketch(int d, int memoryInBytes) {
        this.d = d;
        this.memoryInBytes = memoryInBytes;
        this.w = memoryInBytes / 4 / d;
        this.counters = new int[d][w];
        Random random = new Random();
        this.hashFunctions = (Function<Key, Integer>[]) Stream.generate(random::nextInt)
                .limit(d)
                .map(seed -> (Function<Key, Integer>) (key -> key.hashCode() ^ seed))
                .toArray(Function[]::new);
    }

    public void reset() {
        for (int[] counter: counters) {
            Arrays.fill(counter, 0);
        }
    }

    public int insert(Key key, int f) {
        int count = Integer.MAX_VALUE;
        for (int i = 0; i < d; ++i) {
            int index = hashFunctions[i].apply(key) % w;
            counters[i][index] += f;
            count = Math.min(count, counters[i][index]);
        }
        return count;
    }

    public int insert(Key key) {
        return insert(key, 1);
    }

    public int query(Key key) {
        int count = Integer.MAX_VALUE;
        for (int i = 0; i < d; ++i) {
            int index = hashFunctions[i].apply(key) % w;
            count = Math.min(count, counters[i][index]);
        }
        return count;
    }


}
