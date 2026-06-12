package com.indivaragroup.spring.rest.api.controllers;

import com.indivaragroup.spring.rest.api.dto.request.CreateAccountRequest;
import com.indivaragroup.spring.rest.api.entity.Account;
import com.indivaragroup.spring.rest.api.entity.Customer;
import com.indivaragroup.spring.rest.api.exception.BadRequestException;
import com.indivaragroup.spring.rest.api.exception.ResourceNotFoundException;
import com.indivaragroup.spring.rest.api.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateAccountRequest validRequest;
    private Account account;
    private UUID accountId;
    private String accountNumber;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        accountNumber = "12345678";

        validRequest = CreateAccountRequest.builder()
                .customerId(UUID.randomUUID().toString())
                .accountNumber(accountNumber)
                .balance(new BigDecimal("50000.00"))
                .build();

        Customer customer = Customer.builder()
                .customerId(UUID.fromString(validRequest.getCustomerId()))
                .name("John Doe")
                .email("john@example.com")
                .build();

        account = Account.builder()
                .accountId(accountId)
                .accountNumber(accountNumber)
                .balance(validRequest.getBalance())
                .customer(customer)
                .build();
    }

    @Test
    void createAccount_Success() throws Exception {
        Mockito.when(accountService.create(any(CreateAccountRequest.class))).thenReturn(account);

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId", is(accountId.toString())))
                .andExpect(jsonPath("$.accountNumber", is(accountNumber)))
                .andExpect(jsonPath("$.balance", is(50000.0)));
    }

    @Test
    void createAccount_DuplicateAccountNumber() throws Exception {
        Mockito.when(accountService.create(any(CreateAccountRequest.class)))
                .thenThrow(new BadRequestException("Account number already exists"));

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Account number already exists")));
    }

    @Test
    void createAccount_CustomerNotFound() throws Exception {
        Mockito.when(accountService.create(any(CreateAccountRequest.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Customer not found")));
    }

    @Test
    void getAccountById_Success() throws Exception {
        Mockito.when(accountService.getById(accountId)).thenReturn(account);

        mockMvc.perform(get("/api/v1/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId", is(accountId.toString())))
                .andExpect(jsonPath("$.accountNumber", is(accountNumber)));
    }

    @Test
    void getAccountById_NotFound() throws Exception {
        Mockito.when(accountService.getById(accountId))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(get("/api/v1/accounts/" + accountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Account not found")));
    }

    @Test
    void getAccountByAccountNumber_Success() throws Exception {
        Mockito.when(accountService.getByAccountNumber(accountNumber)).thenReturn(account);

        mockMvc.perform(get("/api/v1/accounts/account-number/" + accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId", is(accountId.toString())))
                .andExpect(jsonPath("$.accountNumber", is(accountNumber)));
    }

    @Test
    void getAccountByAccountNumber_NotFound() throws Exception {
        Mockito.when(accountService.getByAccountNumber(accountNumber))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(get("/api/v1/accounts/account-number/" + accountNumber))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Account not found")));
    }
}
