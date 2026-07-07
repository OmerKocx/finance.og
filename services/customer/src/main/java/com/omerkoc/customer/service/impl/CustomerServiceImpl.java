package com.omerkoc.customer.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.omerkoc.customer.dto.CustomerRequestDto;
import com.omerkoc.customer.dto.CustomerResponseDto;
import com.omerkoc.customer.exception.CustomerAlreadyExistsException;
import com.omerkoc.customer.exception.CustomerNotFoundException;
import com.omerkoc.customer.mapper.CustomerMapper;
import com.omerkoc.customer.model.Customer;
import com.omerkoc.customer.repository.CustomerRepository;
import com.omerkoc.customer.service.ICustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto) {
        log.info("Creating customer with email: {} and name: {}", customerRequestDto.email(),
                customerRequestDto.name());
        if (customerRepository.findFirstByEmail(customerRequestDto.email()).isPresent()) {
            throw new CustomerAlreadyExistsException(
                    "Customer already exists with email: " + customerRequestDto.email());
        }
        Customer customer = customerMapper.toCustomer(customerRequestDto);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer successfully created in MongoDB with ID: {}", savedCustomer.getId());
        return customerMapper.toCustomerResponseDto(savedCustomer);
    }

    @Override
    public List<CustomerResponseDto> listAllCustomers() {
        log.info("Listing all customers");
        List<CustomerResponseDto> customerResponseDtoList = new ArrayList<>();
        for (Customer customer : customerRepository.findAll()) {
            customerResponseDtoList.add(customerMapper.toCustomerResponseDto(customer));
        }
        return customerResponseDtoList;
    }

    @Override
    public CustomerResponseDto getCustomerById(String id) {
        log.info("Getting customer with id: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        return customerMapper.toCustomerResponseDto(customer);
    }

    @Override
    public CustomerResponseDto getCustomerByEmail(String email) {
        Customer customer = customerRepository.findFirstByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
        return customerMapper.toCustomerResponseDto(customer);
    }

    @Override
    public CustomerResponseDto updateCustomer(String id, CustomerRequestDto customerRequestDto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        customer.setName(customerRequestDto.name());
        customer.setEmail(customerRequestDto.email());
        customer.setPhone(customerRequestDto.phone());
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer successfully updated in MongoDB with ID: {}", savedCustomer.getId());
        return customerMapper.toCustomerResponseDto(savedCustomer);
    }

    @Override
    public void deleteCustomer(String id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
        log.info("Customer successfully deleted from MongoDB with ID: {}", id);
    }

    @Override
    public CustomerResponseDto getCustomerByPhone(String phone) {
        log.info("Getting customer with phone: {}", phone);
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with phone: " + phone));
        return customerMapper.toCustomerResponseDto(customer);
    }

}
