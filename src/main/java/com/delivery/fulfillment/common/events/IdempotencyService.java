package com.delivery.fulfillment.common.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for handling event idempotency persistently.
 * Attempts to insert a processed event and returns true if it was processed for the first time.
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
	 * Attempts to register an event as processed.
	 * Returns true if the event had not been processed before, false if it already existed.
	 * 
	 * This method uses an "insert or ignore" strategy based on unique constraint.
	 * If the event already exists, DataIntegrityViolationException is thrown and caught.
	 */
	@Transactional
	public boolean tryProcessEvent(UUID eventId, String eventType, UUID correlationId) {
		try {
			// Attempt to insert the processed event
			ProcessedEvent processedEvent = new ProcessedEvent(
				eventId, 
				CONSUMER_NAME, 
				eventType, 
				correlationId
			);
			processedEventRepository.save(processedEvent);
			
			logger.debug("Event registered as processed: eventId={}, type={}", eventId, eventType);
			return true; // Event processed for the first time
			
		} catch (DataIntegrityViolationException e) {
			// Event was already processed (unique constraint violation)
			logger.warn("Duplicate event detected (already processed): eventId={}, type={}", 
				eventId, eventType);
			return false; // Event already processed
		}
	}
	
	/**
	 * Checks if an event was already processed.
	 */
	public boolean isEventProcessed(UUID eventId) {
		return processedEventRepository.existsByEventId(eventId);
	}
}


