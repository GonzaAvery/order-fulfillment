package com.delivery.fulfillment.order.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tests unitarios para la entidad Order.
 */
class OrderTest {
	
	@Test
	void testCreateOrderWithPlacedStatus() {
		UUID customerId = UUID.randomUUID();
		Order order = new Order(customerId, "Av. Corrientes 1234", BigDecimal.valueOf(1500.50));
		
		assertNotNull(order.getId());
		assertEquals(OrderStatus.PLACED, order.getStatus());
		assertEquals(customerId, order.getCustomerId());
		assertEquals("Av. Corrientes 1234", order.getDeliveryAddress());
		assertEquals(BigDecimal.valueOf(1500.50), order.getTotalAmount());
	}
	
	@Test
	void testValidTransitionFromPlacedToAccepted() {
		Order order = new Order(UUID.randomUUID(), "Address", BigDecimal.TEN);
		order.transitionTo(OrderStatus.ACCEPTED);
		
		assertEquals(OrderStatus.ACCEPTED, order.getStatus());
	}
	
	@Test
	void testInvalidTransitionThrowsException() {
		Order order = new Order(UUID.randomUUID(), "Address", BigDecimal.TEN);
		
		assertThrows(IllegalStateException.class, () -> {
			order.transitionTo(OrderStatus.DELIVERED);
		});
	}
	
	@Test
	void testMarkAsFailed() {
		Order order = new Order(UUID.randomUUID(), "Address", BigDecimal.TEN);
		String reason = "Payment failed";
		
		order.markAsFailed(reason);
		
		assertEquals(OrderStatus.FAILED, order.getStatus());
		assertEquals(reason, order.getFailureReason());
	}
	
	@Test
	void testAssignDelivery() {
		Order order = new Order(UUID.randomUUID(), "Address", BigDecimal.TEN);
		UUID deliveryId = UUID.randomUUID();
		
		order.assignDelivery(deliveryId);
		
		assertEquals(deliveryId, order.getDeliveryId());
	}
}

