package cn.kiko.net_monitor_analysis_system.runner;

import cn.kiko.net_monitor_analysis_system.collector.MonitorDataCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ServerStarter implements CommandLineRunner {
    private final MonitorDataCollector collector;
    @Autowired
    public ServerStarter(MonitorDataCollector collector) {
        this.collector = collector;
    }
    @Override
    public void run(String... args) {
        collector.startCollectorServer();
    }
}
