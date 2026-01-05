package com.delivery.fulfillment.order.events;

import com.delivery.fulfillment.common.events.BaseDomainEvent;

import java.util.UUID;

/**
 * Evento emitido cuando un pedido se completa exitosamente (entregado).
 */
public class OrderCompleted extends BaseDomainEvent {
	
	private final UUID orderId;
	private final UUID deliveryId;
	
	public OrderCompleted(UUID correlationId, UUID orderId, UUID deliveryId) {
		super(correlationId);
		this.orderId = orderId;
		this.deliveryId = deliveryId;
	}
	
	public OrderCompleted(UUID eventId, UUID correlationId, java.time.Instant occurredAt, UUID orderId, UUID deliveryId) {
		super(eventId, correlationId, occurredAt);
		this.orderId = orderId;
		this.deliveryId = deliveryId;
	}
	
	@Override
	public String getEventType() {
		return "OrderCompleted";
	}
	
	public UUID getOrderId() {
		return orderId;
	}
	
	public UUID getDeliveryId() {
		return deliveryId;
	}
}

