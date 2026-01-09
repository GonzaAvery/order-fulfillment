package com.delivery.fulfillment.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new order.
 */
public record CreateOrderRequest(
	@NotNull(message = "Customer ID is required")
	UUID customerId,
	
	@NotBlank(message = "Delivery address is required")
	String deliveryAddress,
	
	@NotNull(message = "Total amount is required")
	@DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
	BigDecimal totalAmount
) {
}

