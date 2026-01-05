package com.delivery.fulfillment.order.events;

import com.delivery.fulfillment.common.events.BaseDomainEvent;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento emitido cuando un usuario crea un pedido.
 */
public class OrderPlaced extends BaseDomainEvent {
	
	private final UUID orderId;
	private final UUID customerId;
	private final String deliveryAddress;
	private final BigDecimal totalAmount;
	
	public OrderPlaced(UUID orderId, UUID customerId, String deliveryAddress, BigDecimal totalAmount) {
		super(orderId); // El orderId actúa como correlationId inicial
		this.orderId = orderId;
		this.customerId = customerId;
		this.deliveryAddress = deliveryAddress;
		this.totalAmount = totalAmount;
	}
	
	public OrderPlaced(UUID eventId, UUID correlationId, java.time.Instant occurredAt,
	                   UUID orderId, UUID customerId, String deliveryAddress, BigDecimal totalAmount) {
		super(eventId, correlationId, occurredAt);
		this.orderId = orderId;
		this.customerId = customerId;
		this.deliveryAddress = deliveryAddress;
		this.totalAmount = totalAmount;
	}
	
	@Override
	public String getEventType() {
		return "OrderPlaced";
	}
	
	public UUID getOrderId() {
		return orderId;
	}
	
	public UUID getCustomerId() {
		return customerId;
	}
	
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
}

