package com.omerkoc.customer.service;

import java.util.List;

import com.omerkoc.customer.dto.CustomerRequestDto;
import com.omerkoc.customer.dto.CustomerResponseDto;

public interface ICustomerService {
    CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto);

    List<CustomerResponseDto> listAllCustomers();

    CustomerResponseDto getCustomerById(String id);

    CustomerResponseDto getCustomerByEmail(String email);

    CustomerResponseDto updateCustomer(String id, CustomerRequestDto customerRequestDto);

    void deleteCustomer(String id);
}
