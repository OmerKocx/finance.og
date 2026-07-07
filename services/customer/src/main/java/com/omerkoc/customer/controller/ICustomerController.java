package com.omerkoc.customer.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.omerkoc.customer.dto.CustomerRequestDto;
import com.omerkoc.customer.dto.CustomerResponseDto;

public interface ICustomerController {

    ResponseEntity<CustomerResponseDto> createCustomer(CustomerRequestDto customerRequestDto);

    ResponseEntity<List<CustomerResponseDto>> listAllCustomers();

    ResponseEntity<CustomerResponseDto> getCustomerById(String id);

    ResponseEntity<CustomerResponseDto> getCustomerByEmail(String email);

    ResponseEntity<CustomerResponseDto> updateCustomer(String id, CustomerRequestDto customerRequestDto);

    ResponseEntity<Void> deleteCustomer(String id);

    ResponseEntity<CustomerResponseDto> getCustomerByPhone(String phone);
}