package com.delivery.fulfillment.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consumidor de eventos de Kafka.
 * Maneja la deserialización, idempotencia y routing de eventos.
 */
@Component
public class EventConsumer {
	
	private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
	
	private final ObjectMapper objectMapper;
	private final FulfillmentEventRouter fulfillmentEventRouter;
	private final NotificationEventRouter notificationEventRouter;
	
	// Cache de eventos procesados para idempotencia (en producción usar Redis/Database)
	private final Map<UUID, Boolean> processedEvents = new ConcurrentHashMap<>();
	
	public EventConsumer(ObjectMapper objectMapper,
	                     FulfillmentEventRouter fulfillmentEventRouter,
	                     NotificationEventRouter notificationEventRouter) {
		this.objectMapper = objectMapper;
		this.fulfillmentEventRouter = fulfillmentEventRouter;
		this.notificationEventRouter = notificationEventRouter;
		
		// Registrar subtipos de eventos para deserialización polimórfica
		registerEventSubtypes();
	}
	
	private void registerEventSubtypes() {
		objectMapper.registerSubtypes(
			new NamedType(com.delivery.fulfillment.order.events.OrderPlaced.class, "OrderPlaced"),
			new NamedType(com.delivery.fulfillment.order.events.OrderAccepted.class, "OrderAccepted"),
			new NamedType(com.delivery.fulfillment.order.events.OrderCompleted.class, "OrderCompleted"),
			new NamedType(com.delivery.fulfillment.order.events.OrderFailed.class, "OrderFailed"),
			new NamedType(com.delivery.fulfillment.delivery.events.CourierAssigned.class, "CourierAssigned"),
			new NamedType(com.delivery.fulfillment.delivery.events.DeliveryDelayed.class, "DeliveryDelayed")
		);
	}
	
	@KafkaListener(topics = "order-events", groupId = "fulfillment-service")
	public void consumeEvent(@Payload String message,
	                         @Header(KafkaHeaders.RECEIVED_KEY) String key,
	                         Acknowledgment acknowledgment) {
		try {
			// Deserializar evento
			DomainEvent event = objectMapper.readValue(message, DomainEvent.class);
			
			// Verificar idempotencia
			if (processedEvents.containsKey(event.getEventId())) {
				logger.warn("Duplicate event detected, skipping: eventId={}, type={}", 
					event.getEventId(), event.getEventType());
				acknowledgment.acknowledge();
				return;
			}
			
			logger.info("Consuming event: type={}, eventId={}, correlationId={}", 
				event.getEventType(), event.getEventId(), event.getCorrelationId());
			
			// Routing de eventos
			fulfillmentEventRouter.route(event);
			notificationEventRouter.route(event);
			
			// Marcar como procesado
			processedEvents.put(event.getEventId(), true);
			
			// Acknowledge después de procesamiento exitoso
			acknowledgment.acknowledge();
			
		} catch (Exception e) {
			logger.error("Error processing event: key={}", key, e);
			// En producción, esto debería enviar a DLQ después de N reintentos
			// Por ahora, no hacemos acknowledge para que Kafka reintente
			// En un sistema real, implementar retry con exponential backoff
		}
	}
}

