package com.delivery.fulfillment.delivery.service;

import com.delivery.fulfillment.common.events.EventPublisher;
import com.delivery.fulfillment.delivery.domain.Courier;
import com.delivery.fulfillment.delivery.domain.Delivery;
import com.delivery.fulfillment.delivery.domain.DeliveryStatus;
import com.delivery.fulfillment.delivery.events.CourierAssigned;
import com.delivery.fulfillment.delivery.events.DeliveryDelayed;
import com.delivery.fulfillment.delivery.repository.CourierRepository;
import com.delivery.fulfillment.delivery.repository.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de gestión de entregas.
 * Maneja el ciclo de vida de las entregas y la asignación de couriers.
 */
@Service
public class DeliveryService {
	
	private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);
	
	private final DeliveryRepository deliveryRepository;
	private final CourierRepository courierRepository;
	private final EventPublisher eventPublisher;
	
	public DeliveryService(DeliveryRepository deliveryRepository, 
	                       CourierRepository courierRepository,
	                       EventPublisher eventPublisher) {
		this.deliveryRepository = deliveryRepository;
		this.courierRepository = courierRepository;
		this.eventPublisher = eventPublisher;
	}
	
	/**
	 * Crea una nueva entrega para un pedido.
	 */
	@Transactional
	public UUID createDelivery(UUID orderId, String deliveryAddress) {
		logger.info("Creating delivery for order: {}", orderId);
		
		Delivery delivery = new Delivery(orderId, deliveryAddress);
		delivery = deliveryRepository.save(delivery);
		
		// Intentar asignar courier automáticamente
		assignCourierIfAvailable(delivery.getId(), delivery.getOrderId());
		
		return delivery.getId();
	}
	
	/**
	 * Asigna un courier disponible a una entrega.
	 */
	@Transactional
	public void assignCourierIfAvailable(UUID deliveryId, UUID orderId) {
		Delivery delivery = deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + deliveryId));
		
		if (delivery.getStatus() != DeliveryStatus.PENDING) {
			logger.warn("Cannot assign courier to delivery {} in status {}", deliveryId, delivery.getStatus());
			return;
		}
		
		List<Courier> availableCouriers = courierRepository.findByAvailableTrue();
		if (availableCouriers.isEmpty()) {
			logger.warn("No available couriers for delivery: {}", deliveryId);
			// En un sistema real, esto podría disparar un retry o notificación
			return;
		}
		
		// Seleccionar el primer courier disponible (lógica simplificada)
		Courier courier = availableCouriers.get(0);
		delivery.assignCourier(courier.getId(), courier.getName());
		deliveryRepository.save(delivery);
		
		// Publicar evento
		CourierAssigned event = new CourierAssigned(
			orderId, // correlationId
			deliveryId,
			orderId,
			courier.getId(),
			courier.getName()
		);
		eventPublisher.publish(event);
		
		logger.info("Courier assigned: deliveryId={}, courierId={}", deliveryId, courier.getId());
	}
	
	/**
	 * Actualiza el estado de una entrega.
	 */
	@Transactional
	public void updateDeliveryStatus(UUID deliveryId, DeliveryStatus newStatus) {
		Delivery delivery = deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + deliveryId));
		
		delivery.transitionTo(newStatus);
		deliveryRepository.save(delivery);
		
		logger.info("Delivery status updated: deliveryId={}, newStatus={}", deliveryId, newStatus);
	}
	
	/**
	 * Simula un retraso en la entrega y publica el evento correspondiente.
	 */
	@Transactional
	public void reportDelay(UUID deliveryId, UUID orderId, Duration delayDuration, String reason) {
		Delivery delivery = deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + deliveryId));
		
		// Publicar evento de retraso
		DeliveryDelayed event = new DeliveryDelayed(
			orderId, // correlationId
			deliveryId,
			orderId,
			delayDuration,
			reason
		);
		eventPublisher.publish(event);
		
		logger.warn("Delivery delay reported: deliveryId={}, delay={}, reason={}", 
			deliveryId, delayDuration, reason);
	}
	
	/**
	 * Marca una entrega como completada.
	 */
	@Transactional
	public void completeDelivery(UUID deliveryId) {
		Delivery delivery = deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + deliveryId));
		
		delivery.transitionTo(DeliveryStatus.DELIVERED);
		deliveryRepository.save(delivery);
		
		logger.info("Delivery completed: deliveryId={}", deliveryId);
	}
	
	/**
	 * Obtiene una entrega por ID de pedido.
	 */
	public Delivery getDeliveryByOrderId(UUID orderId) {
		return deliveryRepository.findByOrderId(orderId)
			.orElseThrow(() -> new IllegalArgumentException("Delivery not found for order: " + orderId));
	}
	
	/**
	 * Obtiene una entrega por su ID.
	 */
	public Delivery getDeliveryById(UUID deliveryId) {
		return deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + deliveryId));
	}
}

