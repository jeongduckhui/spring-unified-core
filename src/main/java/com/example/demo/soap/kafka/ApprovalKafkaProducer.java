package com.example.demo.soap.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovalKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "approval-fallback";

    public void sendFallback(ApprovalFallbackMessage message) {
        kafkaTemplate.send(TOPIC, message);
    }
}