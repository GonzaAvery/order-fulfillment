package com.delivery.fulfillment.common.events;

import com.delivery.fulfillment.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Router de eventos para el Notification Service.
 * Enruta todos los eventos al servicio de notificaciones.
 */
@Component
public class NotificationEventRouter {
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationEventRouter.class);
	
	private final NotificationService notificationService;
	
	public NotificationEventRouter(NotificationService notificationService) {
		this.notificationService = notificationService;
	}
	
	public void route(DomainEvent event) {
		try {
			// El notification service maneja todos los tipos de eventos
			notificationService.handleEvent(event);
		} catch (Exception e) {
			// No re-lanzamos el error para que las notificaciones no bloqueen el procesamiento
			logger.error("Error routing event to notification service: type={}", event.getEventType(), e);
		}
	}
}

