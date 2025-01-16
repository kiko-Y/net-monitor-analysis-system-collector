package cn.kiko.net_monitor_analysis_system.model;

import cn.kiko.net_monitor_analysis_system.algo.IFlowKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportedMonitorData<Key extends IFlowKey> {
    private List<Key> heavyChangeKeys;
    private List<Key> heavyHitterKeys;
    private Integer depthForSizeCM;
    private Integer widthForSizeCM;
    private List<List<Integer>> sizeCM;
    private Integer depthForCountCM;
    private Integer widthForCountCM;
    private List<List<Integer>> countCM;


    @SuppressWarnings("unchecked")
    public static <K extends IFlowKey> ExportedMonitorData<K> parseFromBytes(byte[] bytes, Class<K> clz) {
        int keySize = 0;
        try {
            keySize = (int)clz.getDeclaredMethod("byteSize").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Method parseFromBytes = null;
        try {
            parseFromBytes = clz.getDeclaredMethod("parseFromBytes", byte[].class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int heavyChangeKeysSize = buffer.getInt();
        List<K> heavyChangeKeys = new ArrayList<>();
        for (int i = 0; i < heavyChangeKeysSize; ++i) {
            byte[] keyBytes = new byte[keySize];
            buffer.get(keyBytes);
            try {
                K key = (K) parseFromBytes.invoke(null, keyBytes);
                heavyChangeKeys.add(key);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        int heavyHitterKeysSize = buffer.getInt();
        List<K> heavyHitterKeys = new ArrayList<>();
        for (int i = 0; i < heavyHitterKeysSize; ++i) {
            byte[] keyBytes = new byte[keySize];
            buffer.get(keyBytes);
            try {
                K key = (K) parseFromBytes.invoke(null, keyBytes);
                heavyHitterKeys.add(key);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        int depthForSizeCM = buffer.getInt();
        int widthForSizeCM = buffer.getInt();
        List<List<Integer>> sizeCM = Stream.generate(() -> Stream.generate(buffer::getInt).limit(widthForSizeCM).toList()
        ).limit(depthForSizeCM).toList();
        int depthForCountCM = buffer.getInt();
        int widthForCountCM = buffer.getInt();
        List<List<Integer>> countCM = Stream.generate(() -> Stream.generate(buffer::getInt).limit(widthForSizeCM).toList()
        ).limit(depthForSizeCM).toList();
        return new ExportedMonitorData<>(heavyChangeKeys, heavyHitterKeys,
                depthForSizeCM, widthForSizeCM, sizeCM,
                depthForCountCM, widthForCountCM, countCM);
    }

    public byte[] toBytes(Class<Key> keyClz) {
        int keySize = 0;
        try{
            keySize = (int) keyClz.getDeclaredMethod("byteSize").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        ByteBuffer buffer = ByteBuffer.allocate(4 + heavyChangeKeys.size() * keySize +
                4 + heavyHitterKeys.size() * keySize + 4 * 4 +
                4 * (depthForSizeCM * widthForSizeCM + depthForCountCM * widthForCountCM));
        buffer.putInt(heavyChangeKeys.size());
        for (Key key: heavyChangeKeys) {
            buffer.put(key.convertToBytes());
        }
        buffer.putInt(heavyHitterKeys.size());
        for (Key key: heavyHitterKeys) {
            buffer.put(key.convertToBytes());
        }
        buffer.putInt(depthForSizeCM);
        buffer.putInt(widthForSizeCM);
        for (int i = 0; i < depthForSizeCM; ++i) {
            for (int j = 0; j < widthForSizeCM; ++j) {
                buffer.putInt(sizeCM.get(i).get(j));
            }
        }
        buffer.putInt(depthForCountCM);
        buffer.putInt(widthForCountCM);
        for (int i = 0; i < depthForCountCM; ++i) {
            for (int j = 0; j < widthForCountCM; ++j) {
                buffer.putInt(countCM.get(i).get(j));
            }
        }
        return buffer.array();
    }
}
