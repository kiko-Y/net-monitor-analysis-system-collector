package cn.kiko.net_monitor_analysis_system.data;

import cn.kiko.net_monitor_analysis_system.device.Packet;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketReader {
    // 共 27121713 条数据
    public List<Packet> readAllPacket() {
        return readNPacket(Integer.MAX_VALUE);
    }

    public List<Packet> readNPacket(int n) {
        ClassPathResource resource = new ClassPathResource("./130000.dat");
        if (!resource.exists()) {
            return null;
        }
        List<Packet> packets = new ArrayList<>();
        try (BufferedInputStream ips = new BufferedInputStream(resource.getInputStream())) {
            while (ips.available() > 1) {
                Packet packet = new Packet();
                byte[] bytes = ips.readNBytes(4);
                packet.setSrcIP(((bytes[0] << 24) & 0xFF000000) | ((bytes[1] << 16) & 0xFF0000) | ((bytes[2] << 8) & 0xFF00) | (bytes[3] & 0xFF));
                bytes = ips.readNBytes(4);
                packet.setDstIP(((bytes[0] << 24) & 0xFF000000) | ((bytes[1] << 16) & 0xFF0000) | ((bytes[2] << 8) & 0xFF00) | (bytes[3] & 0xFF));
                bytes = ips.readNBytes(2);
                packet.setSrcPort((short)(((bytes[0] << 8) & 0xFF00) | (bytes[1] & 0xFF)));
                bytes = ips.readNBytes(2);
                packet.setDstPort((short)(((bytes[0] << 8) & 0xFF00) | (bytes[1] & 0xFF)));
                bytes = ips.readNBytes(1);
                packet.setProtocol(bytes[0]);
                // skip timestamp
                ips.skipNBytes(8);
                packets.add(packet);
                if (packets.size() >= n) break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return packets;
    }
}
