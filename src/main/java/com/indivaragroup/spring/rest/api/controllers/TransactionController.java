package com.indivaragroup.spring.rest.api.controllers;

import com.indivaragroup.spring.rest.api.dto.request.TopUpRequest;
import com.indivaragroup.spring.rest.api.dto.request.TransferRequest;
import com.indivaragroup.spring.rest.api.dto.request.WithdrawRequest;
import com.indivaragroup.spring.rest.api.entity.Transaction;
import com.indivaragroup.spring.rest.api.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/topup")
    public Transaction topUp(@Valid @RequestBody TopUpRequest request) {
        return transactionService.topUp(request);
    }

    @PostMapping("/transfer")
    public List<Transaction> transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }

    @PostMapping("/withdraw")
    public Transaction withdraw(@Valid @RequestBody WithdrawRequest request) {
        return transactionService.withdraw(request);
    }

    @GetMapping
    public Page<Transaction> getAll(
            @PageableDefault(sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.getAll(pageable);
    }

    @GetMapping("/account/{accountId}")
    public Page<Transaction> getByAccount(
            @PathVariable UUID accountId,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @PageableDefault(sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.getByAccount(accountId, type, pageable);
    }
}
