package com.delivery.fulfillment.delivery.controller;

import com.delivery.fulfillment.delivery.service.DeliveryService;
import com.delivery.fulfillment.fulfillment.service.FulfillmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para gestión de entregas.
 * Expone endpoints para operaciones de delivery.
 */
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
	
	private final DeliveryService deliveryService;
	private final FulfillmentService fulfillmentService;
	
	public DeliveryController(DeliveryService deliveryService, FulfillmentService fulfillmentService) {
		this.deliveryService = deliveryService;
		this.fulfillmentService = fulfillmentService;
	}
	
	/**
	 * Completa una entrega y finaliza el pedido asociado.
	 * En un sistema real, esto sería llamado por el courier o sistema de tracking.
	 */
	@PostMapping("/{deliveryId}/complete")
	public ResponseEntity<Void> completeDelivery(@PathVariable UUID deliveryId) {
		var delivery = deliveryService.getDeliveryById(deliveryId);
		fulfillmentService.completeOrder(delivery.getOrderId(), deliveryId);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * Obtiene información de una entrega por ID de pedido.
	 */
	@GetMapping("/order/{orderId}")
	public ResponseEntity<DeliveryInfo> getDeliveryByOrderId(@PathVariable UUID orderId) {
		var delivery = deliveryService.getDeliveryByOrderId(orderId);
		return ResponseEntity.ok(new DeliveryInfo(
			delivery.getId(),
			delivery.getOrderId(),
			delivery.getStatus().toString(),
			delivery.getCourierId(),
			delivery.getCourierName()
		));
	}
	
	public record DeliveryInfo(
		UUID id,
		UUID orderId,
		String status,
		UUID courierId,
		String courierName
	) {}
}

