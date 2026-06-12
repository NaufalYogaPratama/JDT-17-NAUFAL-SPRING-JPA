package com.indivaragroup.spring.rest.api.services;

import com.indivaragroup.spring.rest.api.dto.CustomerRequest;
import com.indivaragroup.spring.rest.api.entity.Customer;
import com.indivaragroup.spring.rest.api.exception.EmailAlreadyExistsException;
import com.indivaragroup.spring.rest.api.exception.ResourceNotFoundException;
import com.indivaragroup.spring.rest.api.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;

    @Override
    public Customer create(CustomerRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        return repository.save(customer);
    }

    @Override
    public List<Customer> getAll() {
        return repository.findAll();
    }

    @Override
    public Customer getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    public Customer update(UUID id, CustomerRequest request) {
        Customer customer = getById(id);

        repository.findByEmail(request.getEmail())
                .ifPresent(existingCustomer -> {
                    if (!existingCustomer.getCustomerId().equals(id)) {
                        throw new EmailAlreadyExistsException("Email is already registered");
                    }
                });

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        return repository.save(customer);
    }

    @Override
    public void delete(UUID id) {
        Customer customer = getById(id);
        repository.delete(customer);
    }
}
