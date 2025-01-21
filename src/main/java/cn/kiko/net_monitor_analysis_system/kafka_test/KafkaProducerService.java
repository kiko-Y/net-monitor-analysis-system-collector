package cn.kiko.net_monitor_analysis_system.kafka_test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka-test.topic}")
    private String TEST_KAFKA_TOPIC;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 1000) // 每秒运行一次
    public void send() {
        String message = "Message at " + LocalDateTime.now();
        kafkaTemplate.send(TEST_KAFKA_TOPIC, message);
        System.out.println("Sent message: " + message);
    }
}
