package com.delivery.fulfillment.delivery.controller;

import com.delivery.fulfillment.delivery.service.DeliveryService;
import com.delivery.fulfillment.fulfillment.service.FulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for delivery management.
 * Exposes endpoints for delivery operations.
 */
@RestController
@RequestMapping("/api/deliveries")
@Tag(name = "Deliveries", description = "API for delivery management")
public class DeliveryController {
	
	private final DeliveryService deliveryService;
	private final FulfillmentService fulfillmentService;
	
	public DeliveryController(DeliveryService deliveryService, FulfillmentService fulfillmentService) {
		this.deliveryService = deliveryService;
		this.fulfillmentService = fulfillmentService;
	}
	
	/**
	 * Completes a delivery and finalizes the associated order.
	 * In a real system, this would be called by the courier or tracking system.
	 */
	@PostMapping("/{deliveryId}/complete")
	@Operation(
		summary = "Complete a delivery",
		description = "Marks a delivery as completed and updates the associated order to DELIVERED status. " +
			"Publishes the OrderCompleted event to Kafka."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Delivery completed successfully"
		),
		@ApiResponse(
			responseCode = "404",
			description = "Delivery not found"
		)
	})
	public ResponseEntity<Void> completeDelivery(
		@Parameter(description = "Unique delivery ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
		@PathVariable UUID deliveryId
	) {
		var delivery = deliveryService.getDeliveryById(deliveryId);
		fulfillmentService.completeOrder(delivery.getOrderId(), deliveryId);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * Gets delivery information by order ID.
	 */
	@GetMapping("/order/{orderId}")
	@Operation(
		summary = "Query delivery by order ID",
		description = "Gets the information of the delivery associated with an order, including the assigned courier"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Delivery found",
			content = @Content(schema = @Schema(implementation = DeliveryInfo.class))
		),
		@ApiResponse(
			responseCode = "404",
			description = "Delivery not found for the specified order"
		)
	})
	public ResponseEntity<DeliveryInfo> getDeliveryByOrderId(
		@Parameter(description = "Unique order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
		@PathVariable UUID orderId
	) {
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

