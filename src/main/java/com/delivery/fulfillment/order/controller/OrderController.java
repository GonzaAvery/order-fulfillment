package com.delivery.fulfillment.order.controller;

import com.delivery.fulfillment.order.dto.CreateOrderRequest;
import com.delivery.fulfillment.order.dto.OrderResponse;
import com.delivery.fulfillment.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for order management.
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "API for order management")
public class OrderController {
	
	private final OrderService orderService;
	
	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	
	@PostMapping
	@Operation(
		summary = "Create a new order",
		description = "Creates a new order with PLACED status and publishes the OrderPlaced event to Kafka"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "Order created successfully",
			content = @Content(schema = @Schema(implementation = OrderResponse.class))
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid input data"
		)
	})
	public ResponseEntity<OrderResponse> createOrder(
		@Valid @RequestBody CreateOrderRequest request
	) {
		OrderResponse order = orderService.createOrder(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(order);
	}
	
	@GetMapping("/{orderId}")
	@Operation(
		summary = "Query an order by ID",
		description = "Gets the complete information of an order including its current status"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Order found",
			content = @Content(schema = @Schema(implementation = OrderResponse.class))
		),
		@ApiResponse(
			responseCode = "404",
			description = "Order not found"
		)
	})
	public ResponseEntity<OrderResponse> getOrder(
		@Parameter(description = "Unique order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
		@PathVariable UUID orderId
	) {
		OrderResponse order = orderService.getOrder(orderId);
		return ResponseEntity.ok(order);
	}
}

