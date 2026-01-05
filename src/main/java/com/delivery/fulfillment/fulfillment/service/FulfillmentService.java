package com.delivery.fulfillment.fulfillment.service;

import com.delivery.fulfillment.common.events.EventPublisher;
import com.delivery.fulfillment.delivery.domain.DeliveryStatus;
import com.delivery.fulfillment.delivery.events.CourierAssigned;
import com.delivery.fulfillment.delivery.service.DeliveryService;
import com.delivery.fulfillment.order.domain.OrderStatus;
import com.delivery.fulfillment.order.events.OrderPlaced;
import com.delivery.fulfillment.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de Fulfillment que orquesta el flujo completo del pedido.
 * Consume eventos y coordina acciones entre Order Service y Delivery Service.
 */
@Service
public class FulfillmentService {
	
	private static final Logger logger = LoggerFactory.getLogger(FulfillmentService.class);
	
	private final OrderService orderService;
	private final DeliveryService deliveryService;
	private final EventPublisher eventPublisher;
	
	public FulfillmentService(OrderService orderService, DeliveryService deliveryService, EventPublisher eventPublisher) {
		this.orderService = orderService;
		this.deliveryService = deliveryService;
		this.eventPublisher = eventPublisher;
	}
	
	/**
	 * Procesa el evento OrderPlaced.
	 * Acepta el pedido y crea la entrega asociada.
	 */
	@Transactional
	public void handleOrderPlaced(OrderPlaced event) {
		UUID orderId = event.getOrderId();
		logger.info("Processing OrderPlaced event: orderId={}, correlationId={}", 
			orderId, event.getCorrelationId());
		
		try {
			// 1. Aceptar el pedido
			orderService.updateOrderStatus(orderId, OrderStatus.ACCEPTED);
			
			// 2. Publicar evento OrderAccepted
			com.delivery.fulfillment.order.events.OrderAccepted orderAcceptedEvent = 
				new com.delivery.fulfillment.order.events.OrderAccepted(event.getCorrelationId(), orderId);
			eventPublisher.publish(orderAcceptedEvent);
			
			// 3. Crear la entrega
			UUID deliveryId = deliveryService.createDelivery(orderId, event.getDeliveryAddress());
			
			// 4. Asociar la entrega al pedido
			orderService.assignDelivery(orderId, deliveryId);
			
			logger.info("Order fulfillment initiated: orderId={}, deliveryId={}", orderId, deliveryId);
			
		} catch (Exception e) {
			logger.error("Failed to process OrderPlaced event: orderId={}", orderId, e);
			// Marcar pedido como fallido
			orderService.updateOrderStatus(orderId, OrderStatus.FAILED);
			
			com.delivery.fulfillment.order.events.OrderFailed orderFailedEvent = 
				new com.delivery.fulfillment.order.events.OrderFailed(
					event.getCorrelationId(), 
					orderId, 
					"Failed to process order: " + e.getMessage()
				);
			eventPublisher.publish(orderFailedEvent);
		}
	}
	
	/**
	 * Procesa el evento CourierAssigned.
	 * Actualiza el estado del pedido a PICKED_UP cuando el courier recoge el pedido.
	 */
	@Transactional
	public void handleCourierAssigned(CourierAssigned event) {
		UUID orderId = event.getOrderId();
		logger.info("Processing CourierAssigned event: orderId={}, courierId={}", 
			orderId, event.getCourierId());
		
		// Simular que el courier recoge el pedido después de ser asignado
		// En un sistema real, esto podría ser un evento separado "OrderPickedUp"
		try {
			orderService.updateOrderStatus(orderId, OrderStatus.PICKED_UP);
			deliveryService.updateDeliveryStatus(event.getDeliveryId(), DeliveryStatus.PICKED_UP);
			
			// Simular transición a IN_TRANSIT después de un breve delay
			// En producción, esto sería un evento real cuando el courier inicia el viaje
			orderService.updateOrderStatus(orderId, OrderStatus.IN_TRANSIT);
			deliveryService.updateDeliveryStatus(event.getDeliveryId(), DeliveryStatus.IN_TRANSIT);
			
		} catch (Exception e) {
			logger.error("Failed to process CourierAssigned event: orderId={}", orderId, e);
		}
	}
	
	/**
	 * Completa un pedido cuando la entrega se completa.
	 */
	@Transactional
	public void completeOrder(UUID orderId, UUID deliveryId) {
		logger.info("Completing order: orderId={}, deliveryId={}", orderId, deliveryId);
		
		try {
			// Actualizar estados
			orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);
			deliveryService.completeDelivery(deliveryId);
			
			// Publicar evento de completación
			com.delivery.fulfillment.order.events.OrderCompleted orderCompletedEvent = 
				new com.delivery.fulfillment.order.events.OrderCompleted(orderId, orderId, deliveryId);
			eventPublisher.publish(orderCompletedEvent);
			
		} catch (Exception e) {
			logger.error("Failed to complete order: orderId={}", orderId, e);
		}
	}
}
