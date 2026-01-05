package com.delivery.fulfillment.common.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Interfaz base para todos los eventos de dominio.
 * Garantiza trazabilidad mediante EventId y CorrelationId.
 */
public interface DomainEvent {
	
	/**
	 * Identificador único del evento (idempotencia).
	 */
	UUID getEventId();
	
	/**
	 * Identificador de correlación para trazar el flujo end-to-end.
	 */
	UUID getCorrelationId();
	
	/**
	 * Timestamp de cuando ocurrió el evento.
	 */
	Instant getOccurredAt();
	
	/**
	 * Tipo del evento para routing y deserialización.
	 */
	String getEventType();
}

