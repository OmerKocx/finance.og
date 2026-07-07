package com.omerkoc.auth_service.service;

import com.omerkoc.auth_service.dto.UserLoginEvent;
import com.omerkoc.auth_service.dto.UserRegisterEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendRegisterEvent(UserRegisterEvent event) {
        log.info("Sending user registration event to Kafka: {}", event);
        kafkaTemplate.send("user-registration-topic", event);
    }

    public void sendLoginEvent(UserLoginEvent event) {
        log.info("Sending user login event to Kafka: {}", event);
        kafkaTemplate.send("user-login-topic", event);
    }
}