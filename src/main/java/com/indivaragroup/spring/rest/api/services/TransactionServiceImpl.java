package com.indivaragroup.spring.rest.api.services;

import com.indivaragroup.spring.rest.api.dto.request.TopUpRequest;
import com.indivaragroup.spring.rest.api.dto.request.TransferRequest;
import com.indivaragroup.spring.rest.api.dto.request.WithdrawRequest;
import com.indivaragroup.spring.rest.api.entity.Account;
import com.indivaragroup.spring.rest.api.entity.Transaction;
import com.indivaragroup.spring.rest.api.enums.TransactionType;
import com.indivaragroup.spring.rest.api.exception.BadRequestException;
import com.indivaragroup.spring.rest.api.exception.ResourceNotFoundException;
import com.indivaragroup.spring.rest.api.repositories.AccountRepository;
import com.indivaragroup.spring.rest.api.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Value("${bank.transfer.admin-fee:2500}")
    private BigDecimal adminFee;

    @Override
    @Transactional
    public Transaction topUp(TopUpRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(new BigDecimal("10000")) < 0) {
            throw new BadRequestException("Minimum top up amount is Rp10.000");
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        account.setBalance(balanceAfter);
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.TOPUP)
                .amount(amount)
                .transactionDate(LocalDateTime.now())
                .sourceAccountId(null)
                .destinationAccountId(account.getAccountId())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public List<Transaction> transfer(TransferRequest request) {
        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Account destinationAccount = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        if (sourceAccount.getAccountId().equals(destinationAccount.getAccountId())) {
            throw new BadRequestException("Cannot transfer to same account");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(new BigDecimal("10000")) < 0) {
            throw new BadRequestException("Minimum transfer amount is Rp10.000");
        }

        BigDecimal totalDeduction = amount.add(adminFee);
        if (sourceAccount.getBalance().compareTo(totalDeduction) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        // Deduct from source account
        BigDecimal srcBalanceBefore = sourceAccount.getBalance();
        BigDecimal srcBalanceAfter = srcBalanceBefore.subtract(totalDeduction);
        sourceAccount.setBalance(srcBalanceAfter);
        accountRepository.save(sourceAccount);

        // Add to destination account
        BigDecimal destBalanceBefore = destinationAccount.getBalance();
        BigDecimal destBalanceAfter = destBalanceBefore.add(amount);
        destinationAccount.setBalance(destBalanceAfter);
        accountRepository.save(destinationAccount);

        LocalDateTime transactionDate = LocalDateTime.now();

        // 1. Sender History (TRANSFER_OUT)
        Transaction senderTx = Transaction.builder()
                .transactionType(TransactionType.TRANSFER_OUT)
                .amount(amount)
                .transactionDate(transactionDate)
                .sourceAccountId(sourceAccount.getAccountId())
                .destinationAccountId(destinationAccount.getAccountId())
                .balanceBefore(srcBalanceBefore)
                .balanceAfter(srcBalanceAfter)
                .build();

        // 2. Receiver History (TRANSFER_IN)
        Transaction receiverTx = Transaction.builder()
                .transactionType(TransactionType.TRANSFER_IN)
                .amount(amount)
                .transactionDate(transactionDate)
                .sourceAccountId(sourceAccount.getAccountId())
                .destinationAccountId(destinationAccount.getAccountId())
                .balanceBefore(destBalanceBefore)
                .balanceAfter(destBalanceAfter)
                .build();

        Transaction savedSender = transactionRepository.save(senderTx);
        Transaction savedReceiver = transactionRepository.save(receiverTx);

        return Arrays.asList(savedSender, savedReceiver);
    }

    @Override
    @Transactional
    public Transaction withdraw(WithdrawRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(new BigDecimal("10000")) < 0) {
            throw new BadRequestException("Minimum withdraw amount is Rp10.000");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        account.setBalance(balanceAfter);
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.WITHDRAW)
                .amount(amount)
                .transactionDate(LocalDateTime.now())
                .sourceAccountId(account.getAccountId())
                .destinationAccountId(null)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    public Page<Transaction> getAll(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    public Page<Transaction> getByAccount(UUID accountId, String type, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found");
        }

        String filterType = (type == null || type.trim().isEmpty()) ? "ALL" : type.toUpperCase();
        if (!Arrays.asList("ALL", "TOPUP", "TRANSFER_IN", "TRANSFER_OUT", "WITHDRAW").contains(filterType)) {
            throw new BadRequestException("Invalid transaction type filter: " + type);
        }

        return transactionRepository.findAccountTransactions(accountId, filterType, pageable);
    }
}
