package com.omerkoc.auth_service.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service")
public interface CustomerFeignClient {
    @PostMapping("/customers/api/v1/create")
    void createCustomer(@RequestBody CustomerDto request);

    @GetMapping("/customers/api/v1/email/{email}")
    CustomerDto getCustomerByEmail(@PathVariable("email") String email);
}