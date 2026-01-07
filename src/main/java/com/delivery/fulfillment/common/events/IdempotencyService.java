package com.delivery.fulfillment.common.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio para manejar idempotencia de eventos de manera persistente.
 * Intenta insertar un evento procesado y retorna true si fue procesado por primera vez.
 */
@Service
public class IdempotencyService {
	
	private static final Logger logger = LoggerFactory.getLogger(IdempotencyService.class);
	
	private static final String CONSUMER_NAME = "fulfillment-service";
	
	private final ProcessedEventRepository processedEventRepository;
	
	public IdempotencyService(ProcessedEventRepository processedEventRepository) {
		this.processedEventRepository = processedEventRepository;
	}
	
	/**
	 * Intenta registrar un evento como procesado.
	 * Retorna true si el evento no había sido procesado antes, false si ya existía.
	 * 
	 * Este método usa una estrategia "insert or ignore" basada en constraint único.
	 * Si el evento ya existe, se lanza DataIntegrityViolationException que se captura.
	 */
	@Transactional
	public boolean tryProcessEvent(UUID eventId, String eventType, UUID correlationId) {
		try {
			// Intentar insertar el evento procesado
			ProcessedEvent processedEvent = new ProcessedEvent(
				eventId, 
				CONSUMER_NAME, 
				eventType, 
				correlationId
			);
			processedEventRepository.save(processedEvent);
			
			logger.debug("Event registered as processed: eventId={}, type={}", eventId, eventType);
			return true; // Evento procesado por primera vez
			
		} catch (DataIntegrityViolationException e) {
			// El evento ya fue procesado (violación de constraint único)
			logger.warn("Duplicate event detected (already processed): eventId={}, type={}", 
				eventId, eventType);
			return false; // Evento ya procesado
		}
	}
	
	/**
	 * Verifica si un evento ya fue procesado.
	 */
	public boolean isEventProcessed(UUID eventId) {
		return processedEventRepository.existsByEventId(eventId);
	}
}

