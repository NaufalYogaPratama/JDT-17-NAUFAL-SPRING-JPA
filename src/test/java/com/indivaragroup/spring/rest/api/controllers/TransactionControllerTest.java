package com.indivaragroup.spring.rest.api.controllers;

import com.indivaragroup.spring.rest.api.dto.request.TopUpRequest;
import com.indivaragroup.spring.rest.api.dto.request.TransferRequest;
import com.indivaragroup.spring.rest.api.dto.request.WithdrawRequest;
import com.indivaragroup.spring.rest.api.entity.Transaction;
import com.indivaragroup.spring.rest.api.enums.TransactionType;
import com.indivaragroup.spring.rest.api.exception.BadRequestException;
import com.indivaragroup.spring.rest.api.exception.ResourceNotFoundException;
import com.indivaragroup.spring.rest.api.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private TopUpRequest validTopUpRequest;
    private TransferRequest validTransferRequest;
    private Transaction topUpTransaction;
    private Transaction senderTransaction;
    private Transaction receiverTransaction;
    private UUID accountId1;
    private UUID accountId2;

    @BeforeEach
    void setUp() {
        accountId1 = UUID.randomUUID();
        accountId2 = UUID.randomUUID();

        validTopUpRequest = TopUpRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("15000.00"))
                .build();

        validTransferRequest = TransferRequest.builder()
                .sourceAccountId(accountId1)
                .destinationAccountId(accountId2)
                .amount(new BigDecimal("20000.00"))
                .build();

        topUpTransaction = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .transactionType(TransactionType.TOPUP)
                .amount(validTopUpRequest.getAmount())
                .transactionDate(LocalDateTime.now())
                .destinationAccountId(validTopUpRequest.getAccountId())
                .balanceBefore(new BigDecimal("10000.00"))
                .balanceAfter(new BigDecimal("25000.00"))
                .build();

        senderTransaction = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .transactionType(TransactionType.TRANSFER_OUT)
                .amount(validTransferRequest.getAmount())
                .transactionDate(LocalDateTime.now())
                .sourceAccountId(validTransferRequest.getSourceAccountId())
                .destinationAccountId(validTransferRequest.getDestinationAccountId())
                .balanceBefore(new BigDecimal("50000.00"))
                .balanceAfter(new BigDecimal("27500.00")) // Includes 2500 admin fee deduction
                .build();

        receiverTransaction = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .transactionType(TransactionType.TRANSFER_IN)
                .amount(validTransferRequest.getAmount())
                .transactionDate(LocalDateTime.now())
                .sourceAccountId(validTransferRequest.getSourceAccountId())
                .destinationAccountId(validTransferRequest.getDestinationAccountId())
                .balanceBefore(new BigDecimal("10000.00"))
                .balanceAfter(new BigDecimal("30000.00"))
                .build();
    }

    @Test
    void topUp_Success() throws Exception {
        Mockito.when(transactionService.topUp(any(TopUpRequest.class))).thenReturn(topUpTransaction);

        mockMvc.perform(post("/api/v1/transactions/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTopUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType", is("TOPUP")))
                .andExpect(jsonPath("$.amount", is(15000.0)))
                .andExpect(jsonPath("$.destinationAccountId", is(accountId1.toString())));
    }

    @Test
    void topUp_AccountNotFound() throws Exception {
        Mockito.when(transactionService.topUp(any(TopUpRequest.class)))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(post("/api/v1/transactions/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTopUpRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Account not found")));
    }

    @Test
    void topUp_MinimumAmountFailed() throws Exception {
        Mockito.when(transactionService.topUp(any(TopUpRequest.class)))
                .thenThrow(new BadRequestException("Minimum top up amount is Rp10.000"));

        mockMvc.perform(post("/api/v1/transactions/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTopUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Minimum top up amount is Rp10.000")));
    }

    @Test
    void transfer_Success() throws Exception {
        Mockito.when(transactionService.transfer(any(TransferRequest.class)))
                .thenReturn(Arrays.asList(senderTransaction, receiverTransaction));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].transactionType", is("TRANSFER_OUT")))
                .andExpect(jsonPath("$[1].transactionType", is("TRANSFER_IN")));
    }

    @Test
    void transfer_InsufficientBalance() throws Exception {
        Mockito.when(transactionService.transfer(any(TransferRequest.class)))
                .thenThrow(new BadRequestException("Insufficient balance"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Insufficient balance")));
    }

    @Test
    void transfer_MinimumTransferAmountFailed() throws Exception {
        Mockito.when(transactionService.transfer(any(TransferRequest.class)))
                .thenThrow(new BadRequestException("Minimum transfer amount is Rp10.000"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Minimum transfer amount is Rp10.000")));
    }

    @Test
    void transfer_CannotTransferToSameAccount() throws Exception {
        Mockito.when(transactionService.transfer(any(TransferRequest.class)))
                .thenThrow(new BadRequestException("Cannot transfer to same account"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Cannot transfer to same account")));
    }

    @Test
    void transfer_DestinationAccountNotFound() throws Exception {
        Mockito.when(transactionService.transfer(any(TransferRequest.class)))
                .thenThrow(new ResourceNotFoundException("Destination account not found"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransferRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Destination account not found")));
    }

    @Test
    void getByAccount_Success() throws Exception {
        Mockito.when(transactionService.getByAccount(eq(accountId1), eq("ALL"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(senderTransaction, topUpTransaction)));

        mockMvc.perform(get("/api/v1/transactions/account/" + accountId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].transactionType", is("TRANSFER_OUT")))
                .andExpect(jsonPath("$.content[1].transactionType", is("TOPUP")));
    }

    @Test
    void getByAccount_InvalidFilterType() throws Exception {
        Mockito.when(transactionService.getByAccount(eq(accountId1), eq("INVALID"), any(Pageable.class)))
                .thenThrow(new BadRequestException("Invalid transaction type filter: INVALID"));

        mockMvc.perform(get("/api/v1/transactions/account/" + accountId1 + "?type=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Invalid transaction type filter: INVALID")));
    }

    @Test
    void withdraw_Success() throws Exception {
        WithdrawRequest withdrawRequest = WithdrawRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("10000.00"))
                .build();

        Transaction withdrawTransaction = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .transactionType(TransactionType.WITHDRAW)
                .amount(withdrawRequest.getAmount())
                .transactionDate(LocalDateTime.now())
                .sourceAccountId(accountId1)
                .destinationAccountId(null)
                .balanceBefore(new BigDecimal("50000.00"))
                .balanceAfter(new BigDecimal("40000.00"))
                .build();

        Mockito.when(transactionService.withdraw(any(WithdrawRequest.class))).thenReturn(withdrawTransaction);

        mockMvc.perform(post("/api/v1/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType", is("WITHDRAW")))
                .andExpect(jsonPath("$.amount", is(10000.0)))
                .andExpect(jsonPath("$.sourceAccountId", is(accountId1.toString())))
                .andExpect(jsonPath("$.destinationAccountId", is(nullValue())));
    }

    @Test
    void withdraw_AccountNotFound() throws Exception {
        WithdrawRequest withdrawRequest = WithdrawRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("10000.00"))
                .build();

        Mockito.when(transactionService.withdraw(any(WithdrawRequest.class)))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(post("/api/v1/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Account not found")));
    }

    @Test
    void withdraw_MinimumAmountFailed() throws Exception {
        WithdrawRequest withdrawRequest = WithdrawRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("5000.00"))
                .build();

        Mockito.when(transactionService.withdraw(any(WithdrawRequest.class)))
                .thenThrow(new BadRequestException("Minimum withdraw amount is Rp10.000"));

        mockMvc.perform(post("/api/v1/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Minimum withdraw amount is Rp10.000")));
    }

    @Test
    void withdraw_InsufficientBalance() throws Exception {
        WithdrawRequest withdrawRequest = WithdrawRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("100000.00"))
                .build();

        Mockito.when(transactionService.withdraw(any(WithdrawRequest.class)))
                .thenThrow(new BadRequestException("Insufficient balance"));

        mockMvc.perform(post("/api/v1/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Insufficient balance")));
    }
}
