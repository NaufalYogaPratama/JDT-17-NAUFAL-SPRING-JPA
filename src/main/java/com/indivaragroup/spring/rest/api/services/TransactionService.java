package com.indivaragroup.spring.rest.api.services;

import com.indivaragroup.spring.rest.api.dto.request.TopUpRequest;
import com.indivaragroup.spring.rest.api.dto.request.TransferRequest;
import com.indivaragroup.spring.rest.api.dto.request.WithdrawRequest;
import com.indivaragroup.spring.rest.api.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction topUp(TopUpRequest request);
    List<Transaction> transfer(TransferRequest request);
    Transaction withdraw(WithdrawRequest request);
    Page<Transaction> getAll(Pageable pageable);
    Page<Transaction> getByAccount(UUID accountId, String type, Pageable pageable);
}
