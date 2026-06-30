package com.omerkoc.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * GatewayApplication, API Gateway mikroservisinin ana giriş noktasıdır.
 */
@SpringBootApplication
@EnableDiscoveryClient // Bu servisin Eureka Server'a bir istemci olarak kaydolmasını sağlar
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
