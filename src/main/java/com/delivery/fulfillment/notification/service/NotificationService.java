package com.delivery.fulfillment.notification.service;

import com.delivery.fulfillment.common.events.DomainEvent;
import com.delivery.fulfillment.delivery.events.CourierAssigned;
import com.delivery.fulfillment.delivery.events.DeliveryDelayed;
import com.delivery.fulfillment.order.events.OrderAccepted;
import com.delivery.fulfillment.order.events.OrderCompleted;
import com.delivery.fulfillment.order.events.OrderFailed;
import com.delivery.fulfillment.order.events.OrderPlaced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Notification service (simplified).
 * Simulates sending notifications to the user for relevant events.
 * In a real system, this could integrate with email, SMS, push services, etc.
 */
@Service
public class NotificationService {
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
	
	/**
	 * Processes events and generates notifications for the user.
	 */
	public void handleEvent(DomainEvent event) {
		switch (event.getEventType()) {
			case "OrderPlaced" -> handleOrderPlaced((OrderPlaced) event);
			case "OrderAccepted" -> handleOrderAccepted((OrderAccepted) event);
			case "CourierAssigned" -> handleCourierAssigned((CourierAssigned) event);
			case "DeliveryDelayed" -> handleDeliveryDelayed((DeliveryDelayed) event);
			case "OrderCompleted" -> handleOrderCompleted((OrderCompleted) event);
			case "OrderFailed" -> handleOrderFailed((OrderFailed) event);
			default -> logger.debug("No notification handler for event type: {}", event.getEventType());
		}
	}
	
	private void handleOrderPlaced(OrderPlaced event) {
		logger.info("[NOTIFICATION] Order placed - OrderId: {}, CustomerId: {}, Amount: {}", 
			event.getOrderId(), event.getCustomerId(), event.getTotalAmount());
		// In production: send email/SMS to user confirming the order
	}
	
	private void handleOrderAccepted(OrderAccepted event) {
		logger.info("[NOTIFICATION] Order accepted - OrderId: {}", event.getOrderId());
		// In production: notify user that the order is being processed
	}
	
	private void handleCourierAssigned(CourierAssigned event) {
		logger.info("[NOTIFICATION] Courier assigned - OrderId: {}, Courier: {} ({})", 
			event.getOrderId(), event.getCourierName(), event.getCourierId());
		// In production: notify user with courier information
	}
	
	private void handleDeliveryDelayed(DeliveryDelayed event) {
		logger.warn("[NOTIFICATION] Delivery delayed - OrderId: {}, Delay: {}, Reason: {}", 
			event.getOrderId(), event.getDelayDuration(), event.getReason());
		// In production: notify user about the delay and offer compensation if applicable
	}
	
	private void handleOrderCompleted(OrderCompleted event) {
		logger.info("[NOTIFICATION] Order completed - OrderId: {}, DeliveryId: {}", 
			event.getOrderId(), event.getDeliveryId());
		// In production: send delivery confirmation and request feedback
	}
	
	private void handleOrderFailed(OrderFailed event) {
		logger.error("[NOTIFICATION] Order failed - OrderId: {}, Reason: {}", 
			event.getOrderId(), event.getFailureReason());
		// In production: notify user about the failure and offer refund/retry
	}
}

