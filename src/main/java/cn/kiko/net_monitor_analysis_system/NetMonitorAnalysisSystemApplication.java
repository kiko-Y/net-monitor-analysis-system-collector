package cn.kiko.net_monitor_analysis_system;

import cn.kiko.net_monitor_analysis_system.kafka_test.KafkaConsumerService;
import cn.kiko.net_monitor_analysis_system.kafka_test.KafkaProducerService;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableScheduling
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {KafkaConsumerService.class, KafkaProducerService.class})
})
public class NetMonitorAnalysisSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetMonitorAnalysisSystemApplication.class, args);
    }

}
