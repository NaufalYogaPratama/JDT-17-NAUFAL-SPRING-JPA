package com.indivaragroup.spring.rest.api.controllers;

import com.indivaragroup.spring.rest.api.dto.CustomerRequest;
import com.indivaragroup.spring.rest.api.entity.Customer;
import com.indivaragroup.spring.rest.api.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    // CREATE
    @PostMapping("/create")
    public Customer create(
            @Valid @RequestBody CustomerRequest request) {

        return service.create(request);
    }

    // READ ALL
    @GetMapping("/all")
    public List<Customer> getAll() {

        return service.getAll();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Customer getById(
            @PathVariable UUID id) {

        return service.getById(id);
    }

    // UPDATE
    @PostMapping("/update/{id}")
    public Customer update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request) {

        return service.update(id, request);
    }

    // DELETE
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable UUID id) {

        service.delete(id);

        return "Customer deleted";
    }
}
