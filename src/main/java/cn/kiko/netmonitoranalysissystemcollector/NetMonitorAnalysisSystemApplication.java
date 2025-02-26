package cn.kiko.netmonitoranalysissystemcollector;

import cn.kiko.netmonitoranalysissystemcollector.kafka_test.KafkaConsumerService;
import cn.kiko.netmonitoranalysissystemcollector.kafka_test.KafkaProducerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

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
