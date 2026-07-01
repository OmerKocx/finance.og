package com.omerkoc.auth_service.mapper;

import org.springframework.stereotype.Component;

import com.omerkoc.auth_service.FeignClient.CustomerDto;
import com.omerkoc.auth_service.dto.RegisterRequest;

@Component
public class Mapper {
    public CustomerDto mapToCustomerDto(RegisterRequest request) {
        return new CustomerDto(
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber());
    }
}
