package com.delivery.fulfillment.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Service for registering custom system metrics.
 * Exposes business metrics for observability.
 */
@Service
public class MetricsService {
	
	private final Counter ordersCreatedCounter;
	private final Counter eventsProcessedCounter;
	private final Counter eventsFailedCounter;
	
	public MetricsService(MeterRegistry meterRegistry) {
		this.ordersCreatedCounter = Counter.builder("orders_created_total")
			.description("Total number of orders created")
			.tag("status", "created")
			.register(meterRegistry);
		
		this.eventsProcessedCounter = Counter.builder("events_processed_total")
			.description("Total number of events processed successfully")
			.register(meterRegistry);
		
		this.eventsFailedCounter = Counter.builder("events_failed_total")
			.description("Total number of events that failed to process")
			.register(meterRegistry);
	}
	
	/**
	 * Increments the orders created counter.
	 */
	public void incrementOrdersCreated() {
		ordersCreatedCounter.increment();
	}
	
	/**
	 * Increments the successfully processed events counter.
	 */
	public void incrementEventsProcessed() {
		eventsProcessedCounter.increment();
	}
	
	/**
	 * Increments the failed events counter.
	 */
	public void incrementEventsFailed() {
		eventsFailedCounter.increment();
	}
}


