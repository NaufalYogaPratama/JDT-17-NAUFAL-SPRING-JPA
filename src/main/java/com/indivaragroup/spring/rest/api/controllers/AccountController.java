package com.indivaragroup.spring.rest.api.controllers;

import com.indivaragroup.spring.rest.api.dto.request.CreateAccountRequest;
import com.indivaragroup.spring.rest.api.entity.Account;
import com.indivaragroup.spring.rest.api.services.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public Account create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @GetMapping
    public List<Account> getAll() {
        return accountService.getAll();
    }

    @GetMapping("/{id}")
    public Account getById(@PathVariable UUID id) {
        return accountService.getById(id);
    }

    @GetMapping("/account-number/{accountNumber}")
    public Account getByAccountNumber(@PathVariable String accountNumber) {
        return accountService.getByAccountNumber(accountNumber);
    }
}
