package com.delivery.fulfillment.common.events;

import com.delivery.fulfillment.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Event router for the Notification Service.
 * Routes all events to the notification service.
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
			// The notification service handles all event types
			notificationService.handleEvent(event);
		} catch (Exception e) {
			// We don't re-throw the error so notifications don't block processing
			logger.error("Error routing event to notification service: type={}", event.getEventType(), e);
		}
	}
}

