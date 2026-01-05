package com.delivery.fulfillment.delivery.events;

import com.delivery.fulfillment.common.events.BaseDomainEvent;

import java.util.UUID;

/**
 * Evento emitido cuando se asigna un courier a una entrega.
 */
public class CourierAssigned extends BaseDomainEvent {
	
	private final UUID deliveryId;
	private final UUID orderId;
	private final UUID courierId;
	private final String courierName;
	
	public CourierAssigned(UUID correlationId, UUID deliveryId, UUID orderId, UUID courierId, String courierName) {
		super(correlationId);
		this.deliveryId = deliveryId;
		this.orderId = orderId;
		this.courierId = courierId;
		this.courierName = courierName;
	}
	
	public CourierAssigned(UUID eventId, UUID correlationId, java.time.Instant occurredAt,
	                       UUID deliveryId, UUID orderId, UUID courierId, String courierName) {
		super(eventId, correlationId, occurredAt);
		this.deliveryId = deliveryId;
		this.orderId = orderId;
		this.courierId = courierId;
		this.courierName = courierName;
	}
	
	@Override
	public String getEventType() {
		return "CourierAssigned";
	}
	
	public UUID getDeliveryId() {
		return deliveryId;
	}
	
	public UUID getOrderId() {
		return orderId;
	}
	
	public UUID getCourierId() {
		return courierId;
	}
	
	public String getCourierName() {
		return courierName;
	}
}

