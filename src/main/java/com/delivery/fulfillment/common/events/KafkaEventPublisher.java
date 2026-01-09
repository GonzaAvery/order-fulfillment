package com.delivery.fulfillment.common.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * EventPublisher implementation using Kafka.
 * Serializes events to JSON and publishes them to the corresponding topic.
 */
@Component
public class KafkaEventPublisher implements EventPublisher {
	
	private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
	
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	
	public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}
	
	@Override
	public void publish(DomainEvent event) {
		try {
			String eventJson = objectMapper.writeValueAsString(event);
			String topic = "order-events"; // Single topic for all events
			
			// Use correlationId as key to guarantee order within the same flow
			kafkaTemplate.send(topic, event.getCorrelationId().toString(), eventJson);
			
			logger.info("Published event: type={}, eventId={}, correlationId={}", 
				event.getEventType(), event.getEventId(), event.getCorrelationId());
		} catch (JsonProcessingException e) {
			logger.error("Failed to serialize event: {}", event.getEventType(), e);
			throw new RuntimeException("Failed to publish event", e);
		}
	}
}

