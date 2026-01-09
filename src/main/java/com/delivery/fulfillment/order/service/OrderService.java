package com.delivery.fulfillment.order.service;

import com.delivery.fulfillment.common.events.EventPublisher;
import com.delivery.fulfillment.common.observability.MetricsService;
import com.delivery.fulfillment.order.domain.Order;
import com.delivery.fulfillment.order.domain.OrderStatus;
import com.delivery.fulfillment.order.dto.CreateOrderRequest;
import com.delivery.fulfillment.order.dto.OrderResponse;
import com.delivery.fulfillment.order.events.OrderPlaced;
import com.delivery.fulfillment.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Order management service.
 * Responsible for creating orders and publishing corresponding events.
 */
@Service
public class OrderService {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
	
	private final OrderRepository orderRepository;
	private final EventPublisher eventPublisher;
	private final MetricsService metricsService;
	
	public OrderService(OrderRepository orderRepository, 
	                    EventPublisher eventPublisher,
	                    MetricsService metricsService) {
		this.orderRepository = orderRepository;
		this.eventPublisher = eventPublisher;
		this.metricsService = metricsService;
	}
	
	/**
	 * Creates a new order and publishes the OrderPlaced event.
	 */
	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request) {
		logger.info("Creating order for customer: {}", request.customerId());
		
		Order order = new Order(
			request.customerId(),
			request.deliveryAddress(),
			request.totalAmount()
		);
		
		order = orderRepository.save(order);
		
		// Publish domain event
		OrderPlaced event = new OrderPlaced(
			order.getId(),
			order.getCustomerId(),
			order.getDeliveryAddress(),
			order.getTotalAmount()
		);
		eventPublisher.publish(event);
		
		// Record metric
		metricsService.incrementOrdersCreated();
		
		logger.info("Order created: id={}, status={}", order.getId(), order.getStatus());
		
		return OrderResponse.from(order);
	}
	
	/**
	 * Gets an order by its ID.
	 */
	public OrderResponse getOrder(UUID orderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
		
		return OrderResponse.from(order);
	}
	
	/**
	 * Updates the status of an order.
	 * Used by other services that consume events.
	 */
	@Transactional
	public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
		
		order.transitionTo(newStatus);
		orderRepository.save(order);
		
		logger.info("Order status updated: id={}, newStatus={}", orderId, newStatus);
	}
	
	/**
	 * Associates a delivery to an order.
	 */
	@Transactional
	public void assignDelivery(UUID orderId, UUID deliveryId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
		
		order.assignDelivery(deliveryId);
		orderRepository.save(order);
		
		logger.info("Delivery assigned to order: orderId={}, deliveryId={}", orderId, deliveryId);
	}
}

