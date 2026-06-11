package com.indivaragroup.spring.rest.api.controllers;

import com.indivaragroup.spring.rest.api.dto.CustomerRequest;
import com.indivaragroup.spring.rest.api.entity.Customer;
import com.indivaragroup.spring.rest.api.services.CustomerService;
import com.indivaragroup.spring.rest.api.exception.EmailAlreadyExistsException;
import com.indivaragroup.spring.rest.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerRequest validRequest;
    private Customer customer;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        validRequest = new CustomerRequest();
        validRequest.setName("John Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPhone("081234567890");
        validRequest.setAddress("Sudirman Street No. 12");

        customer = Customer.builder()
                .id(customerId)
                .name(validRequest.getName())
                .email(validRequest.getEmail())
                .phone(validRequest.getPhone())
                .address(validRequest.getAddress())
                .build();
    }

    @Test
    void createCustomer_Success() throws Exception {
        Mockito.when(customerService.create(any(CustomerRequest.class))).thenReturn(customer);

        mockMvc.perform(post("/api/v1/customers/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerId.toString())))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    void createCustomer_ValidationFailed_BlankName() throws Exception {
        CustomerRequest invalidRequest = new CustomerRequest();
        invalidRequest.setName("");
        invalidRequest.setEmail("john.doe@example.com");
        invalidRequest.setPhone("081234567890");
        invalidRequest.setAddress("Sudirman Street No. 12");

        mockMvc.perform(post("/api/v1/customers/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.name", is("Name cannot be blank")));
    }

    @Test
    void createCustomer_ValidationFailed_InvalidEmail() throws Exception {
        CustomerRequest invalidRequest = new CustomerRequest();
        invalidRequest.setName("John Doe");
        invalidRequest.setEmail("not-an-email");
        invalidRequest.setPhone("081234567890");
        invalidRequest.setAddress("Sudirman Street No. 12");

        mockMvc.perform(post("/api/v1/customers/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.email", is("Invalid email format")));
    }

    @Test
    void createCustomer_DuplicateEmail() throws Exception {
        Mockito.when(customerService.create(any(CustomerRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email is already registered"));

        mockMvc.perform(post("/api/v1/customers/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Email is already registered")));
    }

    @Test
    void getCustomerById_Success() throws Exception {
        Mockito.when(customerService.getById(customerId)).thenReturn(customer);

        mockMvc.perform(get("/api/v1/customers/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerId.toString())))
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    void getCustomerById_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        Mockito.when(customerService.getById(randomId))
                .thenThrow(new ResourceNotFoundException("Customer not found with id: " + randomId));

        mockMvc.perform(get("/api/v1/customers/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Customer not found")));
    }
}
