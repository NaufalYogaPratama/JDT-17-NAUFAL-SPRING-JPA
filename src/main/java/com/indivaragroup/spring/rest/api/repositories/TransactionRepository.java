package com.indivaragroup.spring.rest.api.repositories;

import com.indivaragroup.spring.rest.api.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE " +
           "(:type = 'ALL' AND (" +
           "  (t.destinationAccountId = :accountId AND t.transactionType = com.indivaragroup.spring.rest.api.enums.TransactionType.TOPUP) OR " +
           "  (t.sourceAccountId = :accountId AND t.transactionType = com.indivaragroup.spring.rest.api.enums.TransactionType.TRANSFER_OUT) OR " +
           "  (t.destinationAccountId = :accountId AND t.transactionType = com.indivaragroup.spring.rest.api.enums.TransactionType.TRANSFER_IN) OR " +
           "  (t.sourceAccountId = :accountId AND t.transactionType = com.indivaragroup.spring.rest.api.enums.TransactionType.WITHDRAW)" +
           ")) OR " +
           "(:type = 'TOPUP' AND t.destinationAccountId = :accountId AND t.transactionType = com.indivaragroup.spring.rest.api.enums.TransactionType.TOPUP) OR " +
           "(:type = 'TRANSFER_OUT' AND t.sourceAccountId = :accountId AND t.transactionType = com.indivaragroup.spring.rest.api.enums.TransactionType.TRANSFER_OUT) OR " +
           "(:type = 'TRANSFER_IN' AND t.destinationAccountId = :accountId AND t.transactionType = com.indivaragroup.spring.rest.api.enums.TransactionType.TRANSFER_IN) OR " +
           "(:type = 'WITHDRAW' AND t.sourceAccountId = :accountId AND t.transactionType = com.indivaragroup.spring.rest.api.enums.TransactionType.WITHDRAW)")
    Page<Transaction> findAccountTransactions(
            @Param("accountId") UUID accountId,
            @Param("type") String type,
            Pageable pageable);
}
