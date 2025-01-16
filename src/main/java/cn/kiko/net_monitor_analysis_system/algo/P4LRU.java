package cn.kiko.net_monitor_analysis_system.algo;

import cn.kiko.net_monitor_analysis_system.algo.value.ReducibleValue;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class P4LRU<Key, Value extends ReducibleValue> {
    private int lruSize;
    private int maxBucketNum;
    private List<LinkedList<Pair<Key,Value>>> lruCache;

    public P4LRU() {
        this(3, 327680);
    }

    public P4LRU(int lruSize, int maxBucketNum) {
        this.lruSize = lruSize;
        this.maxBucketNum = maxBucketNum;
        lruCache = Stream.generate(LinkedList<Pair<Key, Value>>::new)
                .limit(maxBucketNum)
                .collect(Collectors.toList());
    }

    private Pair<Key, Value> removeLast(LinkedList<Pair<Key, Value>> lru) {
        Pair<Key, Value> evicted = lru.getLast();
        lru.removeLast();
        return evicted;
    }

    public Pair<Key, Value> receiveKV(Key key, Value value) {
        Pair<Key, Value> evicted = null;
        int bucket = key.hashCode() % this.maxBucketNum;
        LinkedList<Pair<Key,Value>> lru = this.lruCache.get(bucket);
        Iterator<Pair<Key, Value>> iter = lru.iterator();
        Value count = null;
        while (iter.hasNext()) {
            Pair<Key, Value> keyWithCount = iter.next();
            if (!keyWithCount.getLeft().equals(key)) continue;
            count = keyWithCount.getRight();
            break;
        }
        if (count != null && !count.isZeroValue()) {
            lru.removeIf(kc -> kc.getLeft().equals(key));
            count.reduce(value);
            lru.addFirst(new ImmutablePair<>(key, count));
        } else {
            if (lru.size() >= this.lruSize) {
                evicted = removeLast(lru);
            }
            lru.addFirst(new ImmutablePair<>(key, value));
        }
        return evicted;
    }

    public List<Pair<Key,Value>> evictAll() {
        List<Pair<Key, Value>> allItems = lruCache.stream().flatMap(List::stream).toList();
        reset();
        return allItems;
    }

    public List<LinkedList<Pair<Key,Value>>> getLruCache() {
        return lruCache;
    }

    public void reset() {
        lruCache = Stream.generate(LinkedList<Pair<Key, Value>>::new)
                .limit(maxBucketNum)
                .collect(Collectors.toList());
    }
}
