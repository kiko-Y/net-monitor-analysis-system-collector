package cn.kiko.netmonitoranalysissystemcollector.config;

import cn.kiko.netmonitoranalysissystemcollector.collector.MonitorDataCollector;
import cn.kiko.switch_sdk.algo.FlowKey2Tuple;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public MonitorDataCollector<FlowKey2Tuple> monitorDataCollector() {
        return new MonitorDataCollector<>(FlowKey2Tuple.class);
    }
}
