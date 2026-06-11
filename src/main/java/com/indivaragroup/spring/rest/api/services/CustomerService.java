package com.indivaragroup.spring.rest.api.services;


import com.indivaragroup.spring.rest.api.dto.CustomerRequest;
import com.indivaragroup.spring.rest.api.entity.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    Customer create(CustomerRequest request);

    List<Customer> getAll();

    Customer getById(UUID id);

    Customer update(UUID id, CustomerRequest request);

    void delete(UUID id);
}
