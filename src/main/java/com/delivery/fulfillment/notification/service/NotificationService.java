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
 * Servicio de notificaciones (simplificado).
 * Simula el envío de notificaciones al usuario ante eventos relevantes.
 * En un sistema real, esto podría integrarse con servicios de email, SMS, push, etc.
 */
@Service
public class NotificationService {
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
	
	/**
	 * Procesa eventos y genera notificaciones para el usuario.
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
		logger.info("📧 [NOTIFICATION] Order placed - OrderId: {}, CustomerId: {}, Amount: {}", 
			event.getOrderId(), event.getCustomerId(), event.getTotalAmount());
		// En producción: enviar email/SMS al usuario confirmando el pedido
	}
	
	private void handleOrderAccepted(OrderAccepted event) {
		logger.info("📧 [NOTIFICATION] Order accepted - OrderId: {}", event.getOrderId());
		// En producción: notificar al usuario que el pedido está siendo procesado
	}
	
	private void handleCourierAssigned(CourierAssigned event) {
		logger.info("📧 [NOTIFICATION] Courier assigned - OrderId: {}, Courier: {} ({})", 
			event.getOrderId(), event.getCourierName(), event.getCourierId());
		// En producción: notificar al usuario con información del courier
	}
	
	private void handleDeliveryDelayed(DeliveryDelayed event) {
		logger.warn("📧 [NOTIFICATION] Delivery delayed - OrderId: {}, Delay: {}, Reason: {}", 
			event.getOrderId(), event.getDelayDuration(), event.getReason());
		// En producción: notificar al usuario sobre el retraso y ofrecer compensación si aplica
	}
	
	private void handleOrderCompleted(OrderCompleted event) {
		logger.info("📧 [NOTIFICATION] Order completed - OrderId: {}, DeliveryId: {}", 
			event.getOrderId(), event.getDeliveryId());
		// En producción: enviar confirmación de entrega y solicitar feedback
	}
	
	private void handleOrderFailed(OrderFailed event) {
		logger.error("📧 [NOTIFICATION] Order failed - OrderId: {}, Reason: {}", 
			event.getOrderId(), event.getFailureReason());
		// En producción: notificar al usuario sobre la falla y ofrecer reembolso/reintento
	}
}

