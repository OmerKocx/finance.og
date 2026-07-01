package com.omerkoc.notification.kafka_consumer;

import com.omerkoc.notification.dto.CustomerResponseDto;
import com.omerkoc.notification.dto.UserLoginEvent;
import com.omerkoc.notification.dto.UserRegisterEvent;
import com.omerkoc.notification.feign.ICustomerClient;
import com.omerkoc.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationConsumer {

    private final EmailService emailService;
    private final ICustomerClient customerClient;

    // 1. Register Listener
    @KafkaListener(topics = "user-registration-topic", groupId = "notification-group")
    public void consumeRegisterEvent(UserRegisterEvent event) {
        log.info("User registration event received: {}", event);

        String subject = "Welcome to Our Finance Application!";
        String body = "Hello " + event.fullName() + ",\n\nYour account has been successfully created.";

        emailService.sendEmail(event.email(), subject, body);
    }

    // 2. Login Listener
    @KafkaListener(topics = "user-login-topic", groupId = "notification-group")
    public void consumeLoginEvent(UserLoginEvent event) {
        log.info("User login event received: {}", event);

        try {
            // Retrieve customer details from customer service using OpenFeign
            Optional<CustomerResponseDto> customerOpt = customerClient.getCustomerByEmail(event.email());

            if (customerOpt.isPresent()) {
                CustomerResponseDto customer = customerOpt.get();
                String fullName = customer.firstName() + " " + customer.lastName();

                String subject = "Security Alert: New Login Detected";
                String body = "Hello " + fullName + ",\n\n" +
                        "A new login has just been detected on your account. If this was not you, please change your password immediately.";

                emailService.sendEmail(customer.email(), subject, body);
            } else {
                log.warn("Customer details not found for email: {}", event.email());
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching customer details or sending mail: ", e);
        }
    }
}
