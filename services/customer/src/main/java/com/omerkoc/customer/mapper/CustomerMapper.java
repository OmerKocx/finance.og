package com.omerkoc.customer.mapper;

import org.springframework.stereotype.Component;

import com.omerkoc.customer.dto.CustomerRequestDto;
import com.omerkoc.customer.dto.CustomerResponseDto;
import com.omerkoc.customer.model.Customer;

@Component
public class CustomerMapper {
    public CustomerResponseDto toCustomerResponseDto(Customer customer) {
        return new CustomerResponseDto(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone());
    }

    public Customer toCustomer(CustomerRequestDto customerRequestDto) {
        Customer customer = new Customer();
        customer.setName(customerRequestDto.name());
        customer.setEmail(customerRequestDto.email());
        customer.setPhone(customerRequestDto.phone());
        return customer;
    }
}