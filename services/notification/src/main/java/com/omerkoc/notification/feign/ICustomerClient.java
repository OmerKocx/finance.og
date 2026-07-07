package com.omerkoc.notification.feign;

import com.omerkoc.notification.dto.CustomerResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "customer-service")
public interface ICustomerClient {

    @GetMapping("/customers/api/v1/get/{id}")
    Optional<CustomerResponseDto> getCustomerById(@PathVariable String id);

    @GetMapping("/customers/api/v1/email/{email}")
    Optional<CustomerResponseDto> getCustomerByEmail(@PathVariable("email") String email);
}