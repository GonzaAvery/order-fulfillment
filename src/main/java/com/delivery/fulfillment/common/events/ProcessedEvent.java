package com.delivery.fulfillment.common.events;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity that records processed events to guarantee idempotency.
 * Prevents duplicate event processing.
 */
@Entity
@Table(name = "processed_events", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "consumer"}))
public class ProcessedEvent {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "event_id", nullable = false, updatable = false)
	private UUID eventId;
	
	@Column(name = "consumer", nullable = false, updatable = false)
	private String consumer;
	
	@Column(name = "processed_at", nullable = false, updatable = false)
	private Instant processedAt;
	
	@Column(name = "event_type")
	private String eventType;
	
	@Column(name = "correlation_id")
	private UUID correlationId;
	
	// Constructor for JPA
	protected ProcessedEvent() {
	}
	
	public ProcessedEvent(UUID eventId, String consumer, String eventType, UUID correlationId) {
		this.eventId = eventId;
		this.consumer = consumer;
		this.eventType = eventType;
		this.correlationId = correlationId;
		this.processedAt = Instant.now();
	}
	
	// Getters
	public UUID getId() {
		return id;
	}
	
	public UUID getEventId() {
		return eventId;
	}
	
	public String getConsumer() {
		return consumer;
	}
	
	public Instant getProcessedAt() {
		return processedAt;
	}
	
	public String getEventType() {
		return eventType;
	}
	
	public UUID getCorrelationId() {
		return correlationId;
	}
}


