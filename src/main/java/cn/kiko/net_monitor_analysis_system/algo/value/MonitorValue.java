package cn.kiko.net_monitor_analysis_system.algo.value;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MonitorValue implements ReducibleValue {
    private int size;
    private int count;

    @Override
    public void reduce(ReducibleValue value) {
        if (!(value instanceof MonitorValue)) {
            throw new RuntimeException("incompatible type");
        }
        size += ((MonitorValue) value).size;
        count += ((MonitorValue) value).count;
    }

    @Override
    public boolean isZeroValue() {
        return count == 0;
    }
}
