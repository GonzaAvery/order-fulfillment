package com.delivery.fulfillment.order.controller;

import com.delivery.fulfillment.order.dto.CreateOrderRequest;
import com.delivery.fulfillment.order.dto.OrderResponse;
import com.delivery.fulfillment.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para gestión de pedidos.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
	
	private final OrderService orderService;
	
	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	
	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		OrderResponse order = orderService.createOrder(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(order);
	}
	
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
		OrderResponse order = orderService.getOrder(orderId);
		return ResponseEntity.ok(order);
	}
}

