package com.delivery.fulfillment.delivery.events;

import com.delivery.fulfillment.common.events.BaseDomainEvent;

import java.time.Duration;
import java.util.UUID;

/**
 * Evento emitido cuando una entrega se retrasa.
 * Permite notificar al usuario y tomar acciones correctivas.
 */
public class DeliveryDelayed extends BaseDomainEvent {
	
	private final UUID deliveryId;
	private final UUID orderId;
	private final Duration delayDuration;
	private final String reason;
	
	public DeliveryDelayed(UUID correlationId, UUID deliveryId, UUID orderId, Duration delayDuration, String reason) {
		super(correlationId);
		this.deliveryId = deliveryId;
		this.orderId = orderId;
		this.delayDuration = delayDuration;
		this.reason = reason;
	}
	
	public DeliveryDelayed(UUID eventId, UUID correlationId, java.time.Instant occurredAt,
	                       UUID deliveryId, UUID orderId, Duration delayDuration, String reason) {
		super(eventId, correlationId, occurredAt);
		this.deliveryId = deliveryId;
		this.orderId = orderId;
		this.delayDuration = delayDuration;
		this.reason = reason;
	}
	
	@Override
	public String getEventType() {
		return "DeliveryDelayed";
	}
	
	public UUID getDeliveryId() {
		return deliveryId;
	}
	
	public UUID getOrderId() {
		return orderId;
	}
	
	public Duration getDelayDuration() {
		return delayDuration;
	}
	
	public String getReason() {
		return reason;
	}
}

