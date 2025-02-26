package cn.kiko.netmonitoranalysissystemcollector.kafka_test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "${kafka-test.topic}")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.printf("Received message: (%s, %s)%n", record.key(), record.value());
    }
}