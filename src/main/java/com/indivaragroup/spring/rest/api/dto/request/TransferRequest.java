package com.indivaragroup.spring.rest.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotNull(message = "Source account ID is required")
    private UUID sourceAccountId;

    @NotNull(message = "Destination account ID is required")
    private UUID destinationAccountId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;
}
