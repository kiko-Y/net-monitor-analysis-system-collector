package cn.kiko.net_monitor_analysis_system.algo;

public interface IFlowKey {
    static int byteSize() {
        throw new RuntimeException("not implemented");
    }
    byte[] convertToBytes();
    static IFlowKey parseFromBytes(byte[] bytes) {
        throw new RuntimeException("not implemented");
    }
}
