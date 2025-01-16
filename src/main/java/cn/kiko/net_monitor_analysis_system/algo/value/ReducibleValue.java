package cn.kiko.net_monitor_analysis_system.algo.value;

public interface ReducibleValue {
    void reduce(ReducibleValue value);

    boolean isZeroValue();
}
