package com.delivery.fulfillment.common.events;

import com.delivery.fulfillment.delivery.events.CourierAssigned;
import com.delivery.fulfillment.fulfillment.service.FulfillmentService;
import com.delivery.fulfillment.order.events.OrderPlaced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Event router for the Fulfillment Service.
 * Routes relevant events to the corresponding service.
 */
@Component
public class FulfillmentEventRouter {
	
	private static final Logger logger = LoggerFactory.getLogger(FulfillmentEventRouter.class);
	
	private final FulfillmentService fulfillmentService;
	
	public FulfillmentEventRouter(FulfillmentService fulfillmentService) {
		this.fulfillmentService = fulfillmentService;
	}
	
	public void route(DomainEvent event) {
		try {
			switch (event.getEventType()) {
				case "OrderPlaced" -> fulfillmentService.handleOrderPlaced((OrderPlaced) event);
				case "CourierAssigned" -> fulfillmentService.handleCourierAssigned((CourierAssigned) event);
				default -> logger.debug("No fulfillment handler for event type: {}", event.getEventType());
			}
		} catch (Exception e) {
			logger.error("Error routing event to fulfillment service: type={}", event.getEventType(), e);
			throw e; // Re-throw so the consumer handles the error
		}
	}
}

