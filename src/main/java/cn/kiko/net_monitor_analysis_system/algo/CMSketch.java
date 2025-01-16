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

    public CMSketch(int d, int memoryInBytes) {
        this.d = d;
        this.memoryInBytes = memoryInBytes;
        this.w = memoryInBytes / 4 / d;
        this.counters = new int[d][w];
        this.hashFunctions = generateHashFunctions(d, 42);
    }

    public Function<Key, Integer>[] generateHashFunctions(int n) {
        return generateHashFunctions(n, 42);
    }

    @SuppressWarnings("unchecked")
    public Function<Key, Integer>[] generateHashFunctions(int n, int seed) {
        int MOD = 0x3f3f3f3f;
        Random random = new Random(seed);
        return (Function<Key, Integer>[]) Stream.generate(random::nextInt)
                .limit(n)
                .map(sd -> (Function<Key, Integer>) (key -> (((key.hashCode() ^ sd) % MOD) + MOD) % MOD))
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
