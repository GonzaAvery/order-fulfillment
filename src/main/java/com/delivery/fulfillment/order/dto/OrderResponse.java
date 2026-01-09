package com.delivery.fulfillment.order.dto;

import com.delivery.fulfillment.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for order query response.
 */
public record OrderResponse(
	UUID id,
	UUID customerId,
	String deliveryAddress,
	BigDecimal totalAmount,
	OrderStatus status,
	String failureReason,
	Instant createdAt,
	Instant updatedAt,
	UUID deliveryId
) {
	public static OrderResponse from(com.delivery.fulfillment.order.domain.Order order) {
		return new OrderResponse(
			order.getId(),
			order.getCustomerId(),
			order.getDeliveryAddress(),
			order.getTotalAmount(),
			order.getStatus(),
			order.getFailureReason(),
			order.getCreatedAt(),
			order.getUpdatedAt(),
			order.getDeliveryId()
		);
	}
}

