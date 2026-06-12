package com.indivaragroup.spring.rest.api.services;

import com.indivaragroup.spring.rest.api.dto.request.CreateAccountRequest;
import com.indivaragroup.spring.rest.api.entity.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    Account create(CreateAccountRequest request);
    Account getById(UUID id);
    Account getByAccountNumber(String accountNumber);
    List<Account> getAll();
}
