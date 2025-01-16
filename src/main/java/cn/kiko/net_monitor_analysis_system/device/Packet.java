package cn.kiko.net_monitor_analysis_system.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Packet {
    private int srcIP;
    private int dstIP;
    private short srcPort;
    private short dstPort;
    private byte protocol;
}
