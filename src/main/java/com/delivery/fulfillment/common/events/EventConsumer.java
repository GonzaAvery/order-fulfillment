package com.delivery.fulfillment.common.events;

import com.delivery.fulfillment.common.observability.MetricsService;
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

import java.util.UUID;

/**
 * Consumidor de eventos de Kafka.
 * Maneja la deserialización, idempotencia persistente y routing de eventos.
 */
@Component
public class EventConsumer {
	
	private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
	
	private final ObjectMapper objectMapper;
	private final FulfillmentEventRouter fulfillmentEventRouter;
	private final NotificationEventRouter notificationEventRouter;
	private final IdempotencyService idempotencyService;
	private final MetricsService metricsService;
	
	public EventConsumer(ObjectMapper objectMapper,
	                     FulfillmentEventRouter fulfillmentEventRouter,
	                     NotificationEventRouter notificationEventRouter,
	                     IdempotencyService idempotencyService,
	                     MetricsService metricsService) {
		this.objectMapper = objectMapper;
		this.fulfillmentEventRouter = fulfillmentEventRouter;
		this.notificationEventRouter = notificationEventRouter;
		this.idempotencyService = idempotencyService;
		this.metricsService = metricsService;
		
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
			
			// Verificar idempotencia persistente
			// Intenta registrar el evento. Si ya existe, retorna false.
			boolean isNewEvent = idempotencyService.tryProcessEvent(
				event.getEventId(), 
				event.getEventType(), 
				event.getCorrelationId()
			);
			
			if (!isNewEvent) {
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
			
			// Registrar métrica de evento procesado exitosamente
			metricsService.incrementEventsProcessed();
			
			// Acknowledge después de procesamiento exitoso
			acknowledgment.acknowledge();
			
		} catch (Exception e) {
			logger.error("Error processing event: key={}", key, e);
			
			// Registrar métrica de evento fallido
			metricsService.incrementEventsFailed();
			
			// En producción, esto debería enviar a DLQ después de N reintentos
			// Por ahora, no hacemos acknowledge para que Kafka reintente
			// En un sistema real, implementar retry con exponential backoff
		}
	}
}

