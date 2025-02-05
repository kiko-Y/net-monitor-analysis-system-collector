package cn.kiko.net_monitor_analysis_system.model;

import cn.kiko.net_monitor_analysis_system.algo.IFlowKey;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.Arrays;


@Data
@AllArgsConstructor
public class SwitchExportedMonitorData<Key extends IFlowKey> {
    // 32 bytes UUID
    private String switchID;
    // 8 bytes
    private Long timeStamp;
    private ExportedMonitorData<Key> exportedMonitorData;

    public static <K extends IFlowKey> SwitchExportedMonitorData<K> parseFromBytes(byte[] bytes, Class<K> clz) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] switchIDInBytes = new byte[32];
        buffer.get(switchIDInBytes);
        String switchID = new String(switchIDInBytes);
        long timeStamp = buffer.getLong();
        return new SwitchExportedMonitorData<>(switchID, timeStamp,
                ExportedMonitorData.parseFromBytes(Arrays.copyOfRange(bytes, 32 + 8, bytes.length), clz));
    }

    public byte[] toBytes(Class<Key> keyClz) {
        byte[] byteOfExportedMonitorData = exportedMonitorData.toBytes(keyClz);
        ByteBuffer buffer = ByteBuffer.allocate(32 + 8 + byteOfExportedMonitorData.length);
        buffer.put(switchID.getBytes());
        buffer.putLong(timeStamp);
        buffer.put(byteOfExportedMonitorData);
        return buffer.array();
    }
}
