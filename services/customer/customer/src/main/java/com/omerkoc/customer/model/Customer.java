package com.omerkoc.customer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "customers")
public class Customer {

    @Id
    private Long id;

    private String name;
    private String email;
    private String phone;
}