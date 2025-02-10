package cn.kiko.net_monitor_analysis_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RealtimeProcessMessage {
    private String switchID;
    private Long timestamp;
}
