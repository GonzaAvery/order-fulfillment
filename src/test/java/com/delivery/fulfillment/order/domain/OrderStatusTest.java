package com.delivery.fulfillment.order.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para validación de transiciones de estado de Order.
 */
class OrderStatusTest {
	
	@Test
	void testPlacedCanTransitionToAccepted() {
		assertTrue(OrderStatus.PLACED.canTransitionTo(OrderStatus.ACCEPTED));
	}
	
	@Test
	void testPlacedCanTransitionToCanceled() {
		assertTrue(OrderStatus.PLACED.canTransitionTo(OrderStatus.CANCELED));
	}
	
	@Test
	void testPlacedCanTransitionToFailed() {
		assertTrue(OrderStatus.PLACED.canTransitionTo(OrderStatus.FAILED));
	}
	
	@Test
	void testPlacedCannotTransitionToDelivered() {
		assertFalse(OrderStatus.PLACED.canTransitionTo(OrderStatus.DELIVERED));
	}
	
	@Test
	void testAcceptedCanTransitionToPickedUp() {
		assertTrue(OrderStatus.ACCEPTED.canTransitionTo(OrderStatus.PICKED_UP));
	}
	
	@Test
	void testAcceptedCanTransitionToCanceled() {
		assertTrue(OrderStatus.ACCEPTED.canTransitionTo(OrderStatus.CANCELED));
	}
	
	@Test
	void testAcceptedCanTransitionToFailed() {
		assertTrue(OrderStatus.ACCEPTED.canTransitionTo(OrderStatus.FAILED));
	}
	
	@Test
	void testPickedUpCanTransitionToInTransit() {
		assertTrue(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.IN_TRANSIT));
	}
	
	@Test
	void testPickedUpCanTransitionToFailed() {
		assertTrue(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.FAILED));
	}
	
	@Test
	void testInTransitCanTransitionToDelivered() {
		assertTrue(OrderStatus.IN_TRANSIT.canTransitionTo(OrderStatus.DELIVERED));
	}
	
	@Test
	void testInTransitCanTransitionToFailed() {
		assertTrue(OrderStatus.IN_TRANSIT.canTransitionTo(OrderStatus.FAILED));
	}
	
	@Test
	void testDeliveredIsTerminal() {
		assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.ACCEPTED));
		assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PICKED_UP));
	}
	
	@Test
	void testCanceledIsTerminal() {
		assertFalse(OrderStatus.CANCELED.canTransitionTo(OrderStatus.ACCEPTED));
	}
	
	@Test
	void testFailedIsTerminal() {
		assertFalse(OrderStatus.FAILED.canTransitionTo(OrderStatus.ACCEPTED));
	}
}

