package com.indivaragroup.spring.rest.api.services;

import com.indivaragroup.spring.rest.api.dto.request.CreateAccountRequest;
import com.indivaragroup.spring.rest.api.entity.Account;
import com.indivaragroup.spring.rest.api.entity.Customer;
import com.indivaragroup.spring.rest.api.exception.BadRequestException;
import com.indivaragroup.spring.rest.api.exception.ResourceNotFoundException;
import com.indivaragroup.spring.rest.api.repositories.AccountRepository;
import com.indivaragroup.spring.rest.api.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Account create(CreateAccountRequest request) {
        UUID customerUuid;
        try {
            customerUuid = UUID.fromString(request.getCustomerId());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Customer not found");
        }

        Customer customer = customerRepository.findById(customerUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (request.getAccountNumber() == null || !request.getAccountNumber().matches("^\\d{8}$")) {
            throw new BadRequestException("Account number must be 8 digits");
        }

        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new BadRequestException("Account number already exists");
        }

        BigDecimal balance = request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO;

        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .balance(balance)
                .customer(customer)
                .build();

        return accountRepository.save(account);
    }

    @Override
    public Account getById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Override
    public Account getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Override
    public List<Account> getAll() {
        return accountRepository.findAll();
    }
}
