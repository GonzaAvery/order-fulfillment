package com.delivery.fulfillment.order.events;

import com.delivery.fulfillment.common.events.BaseDomainEvent;

import java.util.UUID;

/**
 * Evento emitido cuando un pedido falla y no puede completarse.
 */
public class OrderFailed extends BaseDomainEvent {
	
	private final UUID orderId;
	private final String failureReason;
	
	public OrderFailed(UUID correlationId, UUID orderId, String failureReason) {
		super(correlationId);
		this.orderId = orderId;
		this.failureReason = failureReason;
	}
	
	public OrderFailed(UUID eventId, UUID correlationId, java.time.Instant occurredAt, UUID orderId, String failureReason) {
		super(eventId, correlationId, occurredAt);
		this.orderId = orderId;
		this.failureReason = failureReason;
	}
	
	@Override
	public String getEventType() {
		return "OrderFailed";
	}
	
	public UUID getOrderId() {
		return orderId;
	}
	
	public String getFailureReason() {
		return failureReason;
	}
}

