package com.omerkoc.customer.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.omerkoc.customer.controller.ICustomerController;
import com.omerkoc.customer.model.Customer;
import com.omerkoc.customer.service.ICustomerService;

@RestController
@RequestMapping("/customers")
public class CustomerControllerImpl implements ICustomerController {

    @Autowired
    private ICustomerService customerService;

    @Override
    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.saveCustomer(customer);
    }

}
