package com.delivery.fulfillment.order.events;

import com.delivery.fulfillment.common.events.BaseDomainEvent;

import java.util.UUID;

/**
 * Evento emitido cuando el sistema acepta un pedido para procesamiento.
 */
public class OrderAccepted extends BaseDomainEvent {
	
	private final UUID orderId;
	
	public OrderAccepted(UUID correlationId, UUID orderId) {
		super(correlationId);
		this.orderId = orderId;
	}
	
	public OrderAccepted(UUID eventId, UUID correlationId, java.time.Instant occurredAt, UUID orderId) {
		super(eventId, correlationId, occurredAt);
		this.orderId = orderId;
	}
	
	@Override
	public String getEventType() {
		return "OrderAccepted";
	}
	
	public UUID getOrderId() {
		return orderId;
	}
}

