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
 * Servicio de gestión de pedidos.
 * Responsable de crear pedidos y publicar eventos correspondientes.
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
	 * Crea un nuevo pedido y publica el evento OrderPlaced.
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
		
		// Publicar evento de dominio
		OrderPlaced event = new OrderPlaced(
			order.getId(),
			order.getCustomerId(),
			order.getDeliveryAddress(),
			order.getTotalAmount()
		);
		eventPublisher.publish(event);
		
		// Registrar métrica
		metricsService.incrementOrdersCreated();
		
		logger.info("Order created: id={}, status={}", order.getId(), order.getStatus());
		
		return OrderResponse.from(order);
	}
	
	/**
	 * Obtiene un pedido por su ID.
	 */
	public OrderResponse getOrder(UUID orderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
		
		return OrderResponse.from(order);
	}
	
	/**
	 * Actualiza el estado de un pedido.
	 * Usado por otros servicios que consumen eventos.
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
	 * Asocia una entrega a un pedido.
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

