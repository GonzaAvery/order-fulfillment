package com.delivery.fulfillment.common.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Clase base abstracta para eventos de dominio.
 * Proporciona implementación común de EventId, CorrelationId y OccurredAt.
 */
public abstract class BaseDomainEvent implements DomainEvent {
	
	private final UUID eventId;
	private final UUID correlationId;
	private final Instant occurredAt;
	
	protected BaseDomainEvent(UUID correlationId) {
		this.eventId = UUID.randomUUID();
		this.correlationId = correlationId;
		this.occurredAt = Instant.now();
	}
	
	protected BaseDomainEvent(UUID eventId, UUID correlationId, Instant occurredAt) {
		this.eventId = eventId;
		this.correlationId = correlationId;
		this.occurredAt = occurredAt;
	}
	
	@Override
	public UUID getEventId() {
		return eventId;
	}
	
	@Override
	public UUID getCorrelationId() {
		return correlationId;
	}
	
	@Override
	public Instant getOccurredAt() {
		return occurredAt;
	}
	
	@Override
	public abstract String getEventType();
}

