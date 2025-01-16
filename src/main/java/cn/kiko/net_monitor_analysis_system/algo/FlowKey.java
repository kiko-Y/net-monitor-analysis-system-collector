package cn.kiko.net_monitor_analysis_system.algo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowKey implements IFlowKey{
    private int srcIP;
    private int dstIP;
    private short srcPort;
    private short dstPort;
    private byte protocol;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowKey flowKey = (FlowKey) o;
        return srcIP == flowKey.srcIP && dstIP == flowKey.dstIP && srcPort == flowKey.srcPort && dstPort == flowKey.dstPort && protocol == flowKey.protocol;
    }

    @Override
    public int hashCode() {
        // hash = 31 * hash + x.hashcode()
        return Objects.hash(srcIP, dstIP, srcPort, dstPort, protocol);
    }

    public static int byteSize() {
        return 13;
    }

    public static FlowKey parseFromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        FlowKey flowKey = new FlowKey();
        flowKey.setSrcIP(buffer.getInt());
        flowKey.setDstIP(buffer.getInt());
        flowKey.setSrcPort(buffer.getShort());
        flowKey.setDstPort(buffer.getShort());
        flowKey.setProtocol(buffer.get());
        return flowKey;
    }

    @Override
    public byte[] convertToBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(byteSize());
        buffer.putInt(srcIP);
        buffer.putInt(dstIP);
        buffer.putShort(srcPort);
        buffer.putShort(dstPort);
        buffer.put(protocol);
        return buffer.array();
    }
}
