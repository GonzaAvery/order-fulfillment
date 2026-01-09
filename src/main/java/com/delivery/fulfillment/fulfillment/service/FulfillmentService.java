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
 * Fulfillment service that orchestrates the complete order flow.
 * Consumes events and coordinates actions between Order Service and Delivery Service.
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
	 * Processes the OrderPlaced event.
	 * Accepts the order and creates the associated delivery.
	 */
	@Transactional
	public void handleOrderPlaced(OrderPlaced event) {
		UUID orderId = event.getOrderId();
		logger.info("Processing OrderPlaced event: orderId={}, correlationId={}", 
			orderId, event.getCorrelationId());
		
		try {
			// 1. Accept the order
			orderService.updateOrderStatus(orderId, OrderStatus.ACCEPTED);
			
			// 2. Publish OrderAccepted event
			com.delivery.fulfillment.order.events.OrderAccepted orderAcceptedEvent = 
				new com.delivery.fulfillment.order.events.OrderAccepted(event.getCorrelationId(), orderId);
			eventPublisher.publish(orderAcceptedEvent);
			
			// 3. Create the delivery
			UUID deliveryId = deliveryService.createDelivery(orderId, event.getDeliveryAddress());
			
			// 4. Associate the delivery to the order
			orderService.assignDelivery(orderId, deliveryId);
			
			logger.info("Order fulfillment initiated: orderId={}, deliveryId={}", orderId, deliveryId);
			
		} catch (Exception e) {
			logger.error("Failed to process OrderPlaced event: orderId={}", orderId, e);
			// Mark order as failed
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
	 * Processes the CourierAssigned event.
	 * Updates the order status to PICKED_UP when the courier picks up the order.
	 */
	@Transactional
	public void handleCourierAssigned(CourierAssigned event) {
		UUID orderId = event.getOrderId();
		logger.info("Processing CourierAssigned event: orderId={}, courierId={}", 
			orderId, event.getCourierId());
		
		// Simulate that the courier picks up the order after being assigned
		// In a real system, this could be a separate "OrderPickedUp" event
		try {
			orderService.updateOrderStatus(orderId, OrderStatus.PICKED_UP);
			deliveryService.updateDeliveryStatus(event.getDeliveryId(), DeliveryStatus.PICKED_UP);
			
			// Simulate transition to IN_TRANSIT after a brief delay
			// In production, this would be a real event when the courier starts the trip
			orderService.updateOrderStatus(orderId, OrderStatus.IN_TRANSIT);
			deliveryService.updateDeliveryStatus(event.getDeliveryId(), DeliveryStatus.IN_TRANSIT);
			
		} catch (Exception e) {
			logger.error("Failed to process CourierAssigned event: orderId={}", orderId, e);
		}
	}
	
	/**
	 * Completes an order when the delivery is completed.
	 */
	@Transactional
	public void completeOrder(UUID orderId, UUID deliveryId) {
		logger.info("Completing order: orderId={}, deliveryId={}", orderId, deliveryId);
		
		try {
			// Update states
			orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);
			deliveryService.completeDelivery(deliveryId);
			
			// Publish completion event
			com.delivery.fulfillment.order.events.OrderCompleted orderCompletedEvent = 
				new com.delivery.fulfillment.order.events.OrderCompleted(orderId, orderId, deliveryId);
			eventPublisher.publish(orderCompletedEvent);
			
		} catch (Exception e) {
			logger.error("Failed to complete order: orderId={}", orderId, e);
		}
	}
}
