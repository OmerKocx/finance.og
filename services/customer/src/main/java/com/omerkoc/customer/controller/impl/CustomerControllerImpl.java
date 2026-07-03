package com.omerkoc.customer.controller.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omerkoc.customer.controller.ICustomerController;
import com.omerkoc.customer.dto.CustomerRequestDto;
import com.omerkoc.customer.dto.CustomerResponseDto;
import com.omerkoc.customer.service.ICustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customers/api/v1")
@RequiredArgsConstructor
public class CustomerControllerImpl implements ICustomerController {

    private final ICustomerService customerService;

    @Override
    @PostMapping("/create")
    public ResponseEntity<CustomerResponseDto> createCustomer(@RequestBody CustomerRequestDto customerRequestDto) {

        return ResponseEntity.ok(customerService.createCustomer(customerRequestDto));
    }

    @Override
    @GetMapping("/list")
    public ResponseEntity<List<CustomerResponseDto>> listAllCustomers() {
        return ResponseEntity.ok(customerService.listAllCustomers());
    }

    @Override
    @GetMapping("/get/{id}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable("id") String id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Override
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponseDto> getCustomerByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @Override
    @PutMapping("/update/{id}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(@PathVariable("id") String id,
            @RequestBody CustomerRequestDto customerRequestDto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customerRequestDto));
    }

    @Override
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") String id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/phone/{phone}")
    public ResponseEntity<CustomerResponseDto> getCustomerByPhone(@PathVariable("phone") String phone) {
        return ResponseEntity.ok(customerService.getCustomerByPhone(phone));
    }

}
